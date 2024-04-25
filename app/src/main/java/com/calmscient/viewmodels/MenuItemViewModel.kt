package com.calmscient.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calmscient.di.remote.request.MenuItemRequest
import com.calmscient.di.remote.response.MenuItem
import com.calmscient.di.remote.response.MenuItemsResponse
import com.calmscient.repository.MenuItemRepository
import com.calmscient.utils.network.ServerTimeoutHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.net.SocketTimeoutException
import javax.inject.Inject

class MenuItemViewModel @Inject constructor(private val menuItemRepository: MenuItemRepository) : ViewModel() {
    val loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val resultLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val menuItemsLiveData: MutableLiveData<List<MenuItem>> = MutableLiveData()
    val errorLiveData: MutableLiveData<String> = MutableLiveData()
    val failureLiveData: MutableLiveData<String> = MutableLiveData()

    private var lastPlid: Int = -1
    private var lastParentId: Int = -1
    private var lastPatientId: Int = -1
    private var lastClientId: Int = -1

    fun fetchMenuItems(plid: Int, parentId: Int, patientId: Int, clientId: Int) {
        loadingLiveData.value = true // Show loader
        lastPlid = plid
        lastParentId = parentId
        lastPatientId = patientId
        lastClientId = clientId
        viewModelScope.launch {
            try {
                val newUrl = "http://20.197.5.97:8083/identity/menu/fetchMenus"
                menuItemRepository.setBaseUrl(newUrl)
                Log.d("URL", "Request URL: $newUrl")

                val request = MenuItemRequest(plid, parentId, patientId, clientId)

                Log.d("MenuItemViewModel", "Request: $request")
                val response = menuItemRepository.fetchMenuItems(request)
                Log.d("MenuItemViewModel", "Response: $response")
                handleResponse(response)
            }
            catch (e: SocketTimeoutException) {
                // Handle timeout exception
                errorLiveData.postValue("Timeout Exception: ${e.message}")
                Log.e("MenuItemViewModel", "Timeout Exception: ${e.message}")
                resultLiveData.postValue(false) // Indicate login failure
            }
            catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.postValue("Exception: ${e.message}")
                resultLiveData.postValue(false)
            }
            finally {
                loadingLiveData.value = false
            }
        }
    }

    private suspend fun handleResponse(call: Call<MenuItemsResponse>) {
        withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val isSuccess = response.body()?.statusResponse?.responseCode == 200
                    val menuItemsResponse = response.body()
                    if (menuItemsResponse != null && isSuccess) {
                        Log.d("MenuItemViewModel", "Response: $menuItemsResponse")
                        menuItemsLiveData.postValue(menuItemsResponse.menuItems)
                        resultLiveData.postValue(isSuccess)
                    } else {
                        errorLiveData.postValue("Empty response")
                    }
                    resultLiveData.postValue(isSuccess)
                } else {
                    errorLiveData.postValue("Error: ${response.code()}")
                    resultLiveData.postValue(false)
                }
            }
            catch (e: SocketTimeoutException) {
                // Handle timeout exception
                Log.e("MenuItemViewModel", "Timeout Exception: ${e.message}")
                errorLiveData.postValue("Timeout Exception: ${e.message}")
                resultLiveData.postValue(false) // Indicate login failure
            }
            catch (e: Exception) {
                e.printStackTrace()
                failureLiveData.postValue("Exception: ${e.message}")
                resultLiveData.postValue(false)
            }
        }
    }

    // Function to retry fetching menu items
    fun retryFetchMenuItems() {
        fetchMenuItems(lastPlid, lastParentId, lastPatientId, lastClientId)
    }
}
