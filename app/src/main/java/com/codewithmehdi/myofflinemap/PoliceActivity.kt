package com.codewithmehdi.myofflinemap

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.codewithmehdi.myofflinemap.databinding.ActivityPoliceMainBinding
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class PoliceActivity : AppCompatActivity() {

    private lateinit var b: ActivityPoliceMainBinding
    private val client = OkHttpClient()
    private lateinit var adapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPoliceMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Setup RecyclerView
        b.reportsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReportAdapter(listOf())
        b.reportsRecyclerView.adapter = adapter

        loadReports()
    }

    private fun loadReports() {
        val request = Request.Builder()
            .url("http://10.0.2.2/website/loadReport.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    b.textEmpty.text = "Failed to load reports: ${e.message}"
                    b.textEmpty.visibility = View.VISIBLE
                    b.reportsRecyclerView.visibility = View.GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                runOnUiThread {
                    val reports = mutableListOf<Report>()
                    try {
                        val json = JSONObject(body ?: "{}")
                        val arr: JSONArray = json.optJSONArray("reports") ?: JSONArray()
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            reports.add(
                                Report(
                                    id = obj.optInt("id"),
                                    userId = obj.optInt("user_id"),
                                    description = obj.optString("description"),
                                    location = obj.optString("location"),
                                    photoUrl = obj.optString("photo_url"),
                                    status = obj.optString("status"),
                                    timestamp = obj.optString("timestamp"),
                                )
                            )
                        }
                    } catch (e: Exception) {
                        b.textEmpty.text = "Failed to parse reports."
                        b.textEmpty.visibility = View.VISIBLE
                        b.reportsRecyclerView.visibility = View.GONE
                        return@runOnUiThread
                    }

                    if (reports.isEmpty()) {
                        b.textEmpty.text = "No pending traffic accident reports"
                        b.textEmpty.visibility = View.VISIBLE
                        b.reportsRecyclerView.visibility = View.GONE
                    } else {
                        adapter.updateReports(reports)
                        b.textEmpty.visibility = View.GONE
                        b.reportsRecyclerView.visibility = View.VISIBLE
                    }
                }
            }
        })
    }
}

// Define your data class
data class Report(
    val id: Int,
    val userId: Int,
    val description: String,
    val location: String,
    val photoUrl: String?,
    val status: String,
    val timestamp: String,
)

// Adapter skeleton for RecyclerView (implement ViewHolder, onBind, etc.)
class ReportAdapter(private var reports: List<Report>) : androidx.recyclerview.widget.RecyclerView.Adapter<ReportViewHolder>() {

    fun updateReports(newReports: List<Report>) {
        this.reports = newReports
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ReportViewHolder {
        val inflater = android.view.LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun getItemCount(): Int = reports.size

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }
}

class ReportViewHolder(itemView: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    private val textTitle: TextView = itemView.findViewById(R.id.textTitle)
    private val textTime: TextView = itemView.findViewById(R.id.textTime)
    private val textLocation: TextView = itemView.findViewById(R.id.textLocation)
    private val textDescription: TextView = itemView.findViewById(R.id.textDescription)
    private val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
    private val btnValidate: Button = itemView.findViewById(R.id.btnValidate)
    private val btnReject: Button = itemView.findViewById(R.id.btnReject)
    private val btnView: Button = itemView.findViewById(R.id.btnView)

    fun bind(report: Report) {
        // Set title, you can adjust if different report types
        textTitle.text = "Traffic Accident"
        // Set location and description
        textLocation.text = report.location
        textDescription.text = report.description

        // Set time (for demo, just show timestamp. You can format for "5m ago" if you want)
        textTime.text = report.timestamp

        // Optionally set the icon (if you have different types, set appropriate icon)
        imgIcon.setImageResource(R.drawable.ic_accident_report)

        // Optionally set up onClickListeners for actions
        btnValidate.setOnClickListener {
            // Handle validation action
            // e.g., notify adapter or activity
        }
        btnReject.setOnClickListener {
            // Handle rejection action
        }
        btnView.setOnClickListener {
            // Handle view details action
        }
    }
}