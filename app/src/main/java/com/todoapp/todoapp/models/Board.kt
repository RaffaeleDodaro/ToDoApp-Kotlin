package com.todoapp.todoapp.models

import android.os.Parcel
import android.os.Parcelable

/*
    Quando abbiamo la necessità di trasferire un oggetto creato da noi da un'activity ad un'altra
    (oppure anche in altri contesti) dobbiamo ricorrere all'implementazione delle interfacce
    Parcelable e Parcelable.Creator. L'interfaccia Parcelable serve a registrare l'oggetto
    che stiamo trasferendo tramite il metodo writeToParcel() all'interno di un Parcel. Mentre
    l'interfaccia Parcelable.Creator serve a ricostruire l'oggetto.
    L'interfaccia Parcelable descrive la creazione di un oggetto con tutti i suoi dati primitivi tipo int,
    boolean, String...
    I metodi da implementare sono:


    A data class is something that holds the data for us. It doesn't hold any other functionality in it.
    The compiler automatically derives the following members from all properties declared in the primary constructor:

        equals()/ hashCode() pair

        toString() of the form "User(name=John, age=42)"

        componentN() functions corresponding to the properties in their order of declaration.

        copy() function
 */
data class Board(
    val name: String = "",
    val image: String = "",
    val createdBy: String = "",
    val assignedTo: ArrayList<String> = ArrayList(),
    var documentId: String = "",
    var taskList: ArrayList<Task> = ArrayList()
) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!, //!! indicano che non deve essere null
        parcel.readString()!!, //!! indicano che non deve essere null
        parcel.readString()!!, //!! indicano che non deve essere null
        parcel.createStringArrayList()!!, //!! indicano che non deve essere null
        parcel.readString()!!,
        parcel.createTypedArrayList(Task.CREATOR)!!
    )

    // void writeToParcel(Parcel parcel, int flags): questo metodo riceve un oggetto Parcel dove
    // andremo a trasferire gli attributi del nostro oggetto
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeString(createdBy)
        parcel.writeStringList(assignedTo)
        parcel.writeString(documentId)
        parcel.writeTypedList(taskList)
    }

    // int describeContents: questo metodo in genere ritorna zero. Esso viene utilizzato in alcuni
    // casi particolari che non tratteremo in questo articolo
    override fun describeContents(): Int {
        return 0
    }


    companion object CREATOR : Parcelable.Creator<Board> {
        /*
        Questo oggetto serve a ricostruire la classe che implementa Parcelable. I metodi di questa
        interfaccia sono:

        public Point createFromParcel(Parcel source):questo metodo viene invocato automaticamente
            nel momento in cui si tenta di ricostruire un ipotetico oggetto Parcelable. Un caso molto
            comune lo troviamo nella classe Bundle quando invochiamo il metodo getParcelable().

        public Point[] newArray(int size): in questo caso viene ricostruito l'array di un oggetto
            parcelable. Tutto ci sarà piu chiaro nell'esempio che farema fra poco.
         */
        override fun createFromParcel(parcel: Parcel): Board {
            return Board(parcel)
        }

        override fun newArray(size: Int): Array<Board?> {
            return arrayOfNulls(size)
        }
    }
}