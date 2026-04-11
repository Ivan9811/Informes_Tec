package com.example.taller2.Activities.main.productos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taller2.R

import com.example.taller2.Activities.main.productos.ProductoAdapter
import com.example.taller2.Activities.main.productos.product

class HomeFragment : Fragment() {

    private val listaProductos = listOf(
        product("Camisa Casual", 29.9, R.drawable.camisa_casual),
        product("Camisa Casual", 29.9, R.drawable.pantalon_casual),
        product("Camisa Casual", 29.9, R.drawable.zapato_casual),
        product("Camisa Casual", 29.9, R.drawable.chaqueta_casual),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_prodcutos)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = ProductoAdapter(listaProductos)
        return view
    }
}

