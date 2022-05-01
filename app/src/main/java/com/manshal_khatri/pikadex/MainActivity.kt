package com.manshal_khatri.pikadex

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.GestureDetector
import android.view.Menu
import android.view.MotionEvent
import android.view.View.GONE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.manshal_khatri.pikadex.databinding.ActivityMainBinding
import com.manshal_khatri.pikadex.model.*
import com.manshal_khatri.pikadex.util.APIs
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.abs

val pokeApi = APIs.PKMN_API
val pokemonsList = mutableListOf<Pokemons>()                                 // PKMNS
val pokeMoves = mutableListOf<Moves>()                                      // PKMN MOVES
var pokeMoveData = mutableListOf<MoveData>()                              // MOVE DEATILS
var pokeTypeData = mutableListOf<TypesData>()                              //PKMN TYPES DATA
var start = 1
var limit = 5 // (5..15).random()

class MainActivity : AppCompatActivity() {

    var x1 : Float = 0.0f
    var x2 : Float = 0.0f
    var y1 : Float = 0.0f
    var y2 : Float = 0.0f
    lateinit var gd : GestureDetector
    companion object{
        const val TH = 200
    }
   // private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Glide.with(this).load(R.drawable.pikantro).into(binding.imageView2)
        val queue = Volley.newRequestQueue(this)
        val typeQueue = Volley.newRequestQueue(this)
        val movesQueue = Volley.newRequestQueue(this)
//        gd = GestureDetector(this,this)
        /*GlobalScope.launch {*/
           if(pokemonsList.isEmpty()) {
               for (i in start until limit) {

                   val reqPkms = object :
                       JsonObjectRequest(Request.Method.GET, pokeApi + "$i", null, Response.Listener {

                           println("Category API success ${it.getInt("id")}")
                           val pokeObj = it
                           val statsArr = pokeObj.getJSONArray("stats")
                           var si = 0
                           if (pokeObj.getJSONArray("types")
                                   .length() == 2
                           ) {   // IF POKEMON HAS 2 TYPES
                               pokemonsList.add(
                                   Pokemons(
                                       pokeObj.getInt("id"),
                                       pokeObj.getString("name"),
                                       Types(
                                           pokeObj.getJSONArray("types").getJSONObject(0)
                                               .getJSONObject("type")
                                               .getString("name"),
                                           pokeObj.getJSONArray("types").getJSONObject(1)
                                               .getJSONObject("type")
                                               .getString("name")
                                       ),
                                       pokeObj.getJSONObject("sprites").getJSONObject("other")
                                           .getJSONObject("home").getString("front_default"),
                                       Stats(
                                           statsArr.getJSONObject(si++).getInt("base_stat"),
                                           statsArr.getJSONObject(si++).getInt("base_stat"),
                                           statsArr.getJSONObject(si++).getInt("base_stat"),
                                           statsArr.getJSONObject(si++).getInt("base_stat"),
                                           statsArr.getJSONObject(si++).getInt("base_stat"),
                                           statsArr.getJSONObject(si).getInt("base_stat"),
                                       )
                                   ),
                               )
                           } else {
                               pokemonsList.add(
                                   Pokemons(
                                       pokeObj.getInt("id"),
                                       pokeObj.getString("name"),
                                       Types(
                                           pokeObj.getJSONArray("types").getJSONObject(0)
                                               .getJSONObject("type")
                                               .getString("name"), ""
                                       ),
                                       pokeObj.getJSONObject("sprites").getJSONObject("other")
                                           .getJSONObject("home").getString("front_default"),
                                       Stats(
                                           statsArr.getJSONObject(si++).getInt("base_stat"),
                                           statsArr.getJSONObject(si++).getInt("base_stat"),
                                           statsArr.getJSONObject(si++).getInt("base_stat"),
                                           statsArr.getJSONObject(si++).getInt("base_stat"),
                                           statsArr.getJSONObject(si++).getInt("base_stat"),
                                           statsArr.getJSONObject(si).getInt("base_stat"),
                                       )
                                   )
                               )
                           }

                       }, Response.ErrorListener {

                       }) {

                   }
                   queue.add(reqPkms)
               }
           }
            for(i in 1 until APIs.LAST_TYPE){
            val reqTypes = object :
                JsonObjectRequest(Request.Method.GET, APIs.TYPES_API + "$i", null, Response.Listener {
                    println(" $i type fetching succeed")
                    val ad = it.getJSONObject("damage_relations").getJSONArray("double_damage_from")
                    val adL = mutableListOf<String>()
                    val dd = it.getJSONObject("damage_relations").getJSONArray("half_damage_from")
                    val ddL = mutableListOf<String>()
                    val nd = it.getJSONObject("damage_relations").getJSONArray("no_damage_from")
                    val ndL = mutableListOf<String>()
                    var x =0
                    var y = 0
                    var z = 0
                    while(x < ad.length() || y < dd.length() || z < nd.length()){
                        if(x<ad.length()){
                            ddL.add(ad.getJSONObject(x).getString("name"))
                            x++
                        }
                        if(y < dd.length()){
                            adL.add(dd.getJSONObject(y).getString("name"))
                            y++
                        }
                        if(z < nd.length()){
                            ndL.add(nd.getJSONObject(z).getString("name"))
                            z++
                        }
                    }
                    pokeTypeData.add(
                        TypesData(
                            it.getInt("id"),
                            it.getString("name"),adL,ddL,ndL
                        )
                    )
                },
                    Response.ErrorListener {
                        println("type fetching error")
                    }){}
            typeQueue.add(reqTypes)
        }
            for(i in 1 until APIs.MOVE_LIMIT){
                val reqTypes = object :
                    JsonObjectRequest(Request.Method.GET, APIs.Moves_API + "$i", null, Response.Listener { jsonObject ->
                        println(" $i move fetching succeed")
                        val kind =  jsonObject.getJSONObject("damage_class").getString("name")
                        var power : Int
                        var acc : Int
                        try {
                            power = jsonObject.getInt("power")
                        }catch (e : Exception){
                            power = 0
                        }
                        try {
                            acc = jsonObject.getInt("accuracy")
                        }catch (e : Exception){
                            acc = 0
                        }
                        try {
                            pokeMoveData.add(
                                MoveData(
                                    jsonObject.getInt("id"),
                                    jsonObject.getString("name"),
                                    power,
                                    acc,
                                    jsonObject.getInt("pp"),
                                    jsonObject.getJSONObject("type").getString("name"),kind
                                )
                            )
                        }catch (e: Exception){
                            println(e)
                        }
                    },
                        Response.ErrorListener {
                            println("move fetching error")
                        }){}
                movesQueue.add(reqTypes)
            }
       }

        Handler().postDelayed({
            binding.imageView2.visibility = GONE
            binding.pokeListContainer.background = null
            supportFragmentManager.beginTransaction().replace(R.id.pokeList_container, DashboardFragment()).commit()
        },5000)

        binding.button.setOnClickListener {
           supportFragmentManager.beginTransaction().replace(R.id.pokeList_container, DashboardFragment()).commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

 /*   override fun onDown(e: MotionEvent?): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent?) {


    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {

        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {

        return false
    }

    override fun onLongPress(e: MotionEvent?) {

    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gd.onTouchEvent(event)
        when(event?.action){
            0 ->{
                x1 = event.x
                y1=event.y
            }
            1-> {
                x2 = event.x
                y2=event.y
                val v1 : Float = x2-x1
                val v2 : Float = y2-y1
                if(abs(v1) > TH){
                    if(x2 > x1){
                        println("RIGHT")
                        Toast.makeText(this, "Right", Toast.LENGTH_SHORT).show()
                    }else{
                        println("LEFT")
                        Toast.makeText(this, "Left", Toast.LENGTH_SHORT).show()
                    }
                }else if(abs(v2) > TH){
                    if(y2 > y1){
                        println("BOTTOM")
                        Toast.makeText(this, "Bottom", Toast.LENGTH_SHORT).show()
                    }else{
                        println("TOP")
                        Toast.makeText(this, "Top", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }*/

}