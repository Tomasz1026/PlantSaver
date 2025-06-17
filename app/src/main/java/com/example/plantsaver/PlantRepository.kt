package com.example.plantsaver

class PlantRepository(private val db: LocalDB) {
    suspend fun getAllPlants() = db.plantDao().getAll()
    suspend fun addPlant(name:String, species:String, frequency: Int) = db.plantDao().insert(name, species, frequency)
    suspend fun addPlant(name:String, species:String, date:Long, frequency: Int) = db.plantDao().insert(name, species, date, frequency)
    suspend fun removePlant(plant: Plant) = db.plantDao().delete(plant)
    suspend fun getPlantHistory(plantId: Int) = db.wateringDao().getPlantHistory(plantId)
    suspend fun getPlantsToWater() = db.plantDao().getPlantsToWater()
    suspend fun addWatering(plantId:Int, date: Long) = db.wateringDao().insert(plantId, date)
    suspend fun addWatering(plantId:Int) = db.wateringDao().insert(plantId)
    //suspend fun removeWatering(watering: Watering) = db.wateringDao().delete(watering)
    suspend fun deleteAllPlant() = db.plantDao().deleteAll()
    suspend fun deleteAllWatering() = db.wateringDao().deleteAll()
    suspend fun getLastUserLocation() = db.userDataDao().getLastUserLocation()
    suspend fun addUserLocation() = db.userDataDao().insert(UserData(1, 52.40, 16.93))
    suspend fun updateUserLocation(latitude:Double, longitude:Double) = db.userDataDao().updateUserLocation(
         latitude, longitude)
}