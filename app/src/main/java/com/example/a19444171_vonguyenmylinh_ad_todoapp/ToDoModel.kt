package com.example.a19444171_vonguyenmylinh_ad_todoapp

class ToDoModel {
    companion object Factory {
        fun createList(): ToDoModel = ToDoModel()
    }

    var uID: String? = null
    var itemDataText: String? = null
    var status: Boolean? = false
}