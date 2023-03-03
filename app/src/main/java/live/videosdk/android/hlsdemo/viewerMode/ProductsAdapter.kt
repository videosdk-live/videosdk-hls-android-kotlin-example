package live.videosdk.android.hlsdemo.viewerMode

import org.json.JSONArray
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import live.videosdk.android.hlsdemo.R
import org.json.JSONException
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductsAdapter(private val items: JSONArray) :
    RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_products, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val jsonObject = items.getJSONObject(position)
            holder.productName.text = jsonObject.getString("productName")
            holder.productPrice.text = "$".plus(jsonObject.getString("productPrice"))
            holder.productImage.setImageResource(jsonObject.getInt("productImage"))
            holder.btnBuy.setOnClickListener {
                //
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return items.length()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var productImage: ImageView
        var productName: TextView
        var productPrice: TextView
        var btnBuy: Button

        init {
            productImage = itemView.findViewById(R.id.productImage)
            productName = itemView.findViewById(R.id.productName)
            productPrice = itemView.findViewById(R.id.productPrice)
            btnBuy = itemView.findViewById(R.id.btnBuy)
        }
    }
}