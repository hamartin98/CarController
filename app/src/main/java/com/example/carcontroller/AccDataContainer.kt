package com.example.carcontroller

class AccDataContainer{
    //private var xContainer = mutableListOf<Double>()
    //private var yContainer = mutableListOf<Double>()
    //private var sContainer = mutableListOf<String>()
    private var str: String = "alap"

    fun addValues(s: String) // érték hozzáadása a tárolóhoz
    {
        str = s
        //sContainer.add(s)
    }

    fun getStr(): String // Az utolsó s érték visszaadása
    {
        return str
    }

    /*
    private fun getLastX(): Double
    {
        return xContainer.last()
    }
    */

    /*
    private fun getLastY(): Double
    {
        return yContainer.last()
    }
    */

    /*
    fun getLastS(): String
    {
        return sContainer.last()
    }
    */



    /*
    fun deleteFirst()
    {
        sContainer.removeAt(0)
    }
    */
}