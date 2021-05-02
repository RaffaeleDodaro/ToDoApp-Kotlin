
import android.os.Parcel
import android.os.Parcelable
import com.todoapp.todoapp.models.Task

data class Board(
        val name:String="",
        val image: String ="",
        val createdBy:String="",
        val assignedTo:ArrayList<String> = ArrayList(),
        var documentId:String="",
        var taskList:ArrayList<Task> = ArrayList()
):
        Parcelable{
    constructor(parcel: Parcel):this(
            parcel.readString()!!, //!! indicano che non deve essere null
            parcel.readString()!!, //!! indicano che non deve essere null
            parcel.readString()!!, //!! indicano che non deve essere null
            parcel.createStringArrayList()!!, //!! indicano che non deve essere null
            parcel.readString()!!,
            parcel.createTypedArrayList(Task.CREATOR)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeString(createdBy)
        parcel.writeStringList(assignedTo)
        parcel.writeString(documentId)
        parcel.writeTypedList(taskList)
    }

    override fun describeContents(): Int {
        return 0
    }


    companion object CREATOR : Parcelable.Creator<Board> {
        override fun createFromParcel(parcel: Parcel): Board {
            return Board(parcel)
        }

        override fun newArray(size: Int): Array<Board?> {
            return arrayOfNulls(size)
        }
    }
}