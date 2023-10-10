package com.feylabs.poke.data.repository

import android.net.ConnectivityManager
import com.feylabs.core.helper.error.ErrorResponse
import com.feylabs.core.helper.network.NetworkInfo
import com.feylabs.core.helper.wrapper.ResponseState
import com.feylabs.poke.data.RemoteDataSource
import com.feylabs.poke.data.mapper.Mapper.toPokemonDetailUiModel
import com.feylabs.poke.data.mapper.Mapper.toPokemonEntity
import com.feylabs.poke.data.mapper.Mapper.toPokemonUiModel
import com.feylabs.poke.data.source.local.dao.PokemonDao
import com.feylabs.poke.data.source.local.entity.PokemonEntity
import com.feylabs.poke.di.PokemonModule.ConnectivityManagerSnips
import com.feylabs.poke.domain.repository.PokemonRepository
import com.feylabs.poke.domain.uimodel.PokemonDetailUiModel
import com.feylabs.poke.domain.uimodel.PokemonUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

class PokemonRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    @ConnectivityManagerSnips private val connectivityManager: ConnectivityManager,
    private val pokeDb: PokemonDao,
) : PokemonRepository {


    override fun getPokemon(query: String) =
        flow {
            emit(ResponseState.Loading())
            Timber.d("case12 p0 query is $query")
            if (!NetworkInfo.isOnline(connectivityManager)) {
                Timber.d("case12 p1 user is offline")
                val errorMessage = "Tidak Ada Koneksi Internet"
                val data = pokeDb.findPokemonByName(query).map { it.toPokemonUiModel() }
                emit(ResponseState.Error(ErrorResponse(errorMessage = errorMessage)))
                delay(1000)
                emit(ResponseState.Success(data))

                if(query.isNotEmpty()){
                    val dataQ: List<PokemonUiModel> = if(query.isEmpty()){
                        pokeDb.getAllPokemon().map { it.toPokemonUiModel() }
                    }else{
                        pokeDb.findPokemonByName(query).map { it.toPokemonUiModel() }
                    }
                    emit(ResponseState.Success(dataQ))
                }
                return@flow

            }else{
                Timber.d("case12 p2 user is online")
            }

            try {
                val response = remoteDataSource.getPokemon()
                Timber.d("case12 p3 fetch data from network")
                if (response.isSuccessful) {
                    response.body()?.results?.map { it.toPokemonEntity() }?.let {
                        pokeDb.insertAllPokemon(it)
                    }
                    Timber.d("case12 p4 network call success ")
                    delay(1000)
                    val data: List<PokemonUiModel> = if(query.isEmpty()){
                        pokeDb.getAllPokemon().map { it.toPokemonUiModel() }
                    }else{
                        Timber.d("case12 p4b load with filter ")
                        pokeDb.findPokemonByName(query).map { it.toPokemonUiModel() }
                    }

                    Timber.d("case12 p5 network call success ${data.size} ")
                    emit(ResponseState.Success(data))
                } else {
                    Timber.d("case12 p6 network call error")
                    val errorResponse = ErrorResponse(response.message().toString())
                    emit(ResponseState.Error(errorResponse))
                }

            } catch (e: Exception) {
                Timber.d("case12 p7 network call exception")
                val errorMessage = "Tidak Ada Koneksi Internet"
                val data = pokeDb.findPokemonByName(query).map { it.toPokemonUiModel() }
                emit(ResponseState.Error(ErrorResponse(errorMessage = errorMessage)))
                delay(1000)
                emit(ResponseState.Success(data))
            }
        }.flowOn(Dispatchers.IO)

    override fun getDetailPokemon(name:String): Flow<ResponseState<PokemonDetailUiModel>> =
        flow {
            emit(ResponseState.Loading())
            try {
                val response = remoteDataSource.getPokemonDetail(
                    name
                )
                Timber.d("case12 p3 fetch data from network")
                if (response.isSuccessful) {
                    if (response.body()!=null){
                        val data = response.body()?.toPokemonDetailUiModel()
                        Timber.d("case12 p5 network call success ${response.body()} ")
                        emit(ResponseState.Success(data))
                    }else{
                        emit(ResponseState.Error(ErrorResponse("Pokemon Not Found")))
                    }
                } else {
                    Timber.d("case12 p6 network call error")
                    val errorResponse = ErrorResponse(response.message().toString())
                    emit(ResponseState.Error(errorResponse))
                }

            } catch (e: Exception) {
                Timber.d("case12 p7 network call exception")
                emit(ResponseState.Error(ErrorResponse(errorMessage = e.message.orEmpty())))
            }
        }.flowOn(Dispatchers.IO)



}



