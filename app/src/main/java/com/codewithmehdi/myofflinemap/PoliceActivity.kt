package com.codewithmehdi.myofflinemap

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
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
        adapter = ReportAdapter(listOf(),
            onValidate = { report -> updateReportStatus(report, "confirmed") },
            onReject = { report -> updateReportStatus(report, "rejected") },
            onView = { report -> showReportDetail(report) }
        )
        b.reportsRecyclerView.adapter = adapter

        // Hide detail on close
        b.btnCloseDetail.setOnClickListener {
            b.layoutReportDetail.visibility = View.GONE
        }

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

    fun showReportDetail(report: Report) {
        b.layoutReportDetail.visibility = View.VISIBLE
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        b.layoutReportDetail.startAnimation(slideUp)

        val photoUrl = report.photoUrl
        if (!photoUrl.isNullOrEmpty()) {
            // Ensure there is exactly one slash between baseUrl and photoUrl
            val baseUrl = "http://10.0.2.2/website"
            val fullUrl = if (photoUrl.startsWith("http")) {
                photoUrl
            } else if (photoUrl.startsWith("/")) {
                baseUrl + photoUrl
            } else {
                "$baseUrl/$photoUrl"
            }

            Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.report_circle)
                .error(R.drawable.report_circle)
                .into(b.imgResidentUpload)
        } else {
            b.imgResidentUpload.setImageResource(R.drawable.report_circle)
        }
    }

    fun updateReportStatus(report: Report, newStatus: String) {
        // Optionally show loading indicator...

        val requestBody = FormBody.Builder()
            .add("report_id", report.id.toString())
            .add("status", newStatus)
            .build()

        val request = Request.Builder()
            .url("http://10.0.2.2/website/updateReportStatus.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@PoliceActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    loadReports() // Reload list after update
                    Toast.makeText(this@PoliceActivity, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                    b.layoutReportDetail.visibility = View.GONE // Hide detail if open
                }
            }
        })
    }
}

// Data class
data class Report(
    val id: Int,
    val userId: Int,
    val description: String,
    val location: String,
    val photoUrl: String?,
    val status: String,
    val timestamp: String,
)

// Adapter
class ReportAdapter(
    private var reports: List<Report>,
    private val onValidate: (Report) -> Unit,
    private val onReject: (Report) -> Unit,
    private val onView: (Report) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<ReportViewHolder>() {

    fun updateReports(newReports: List<Report>) {
        this.reports = newReports
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ReportViewHolder {
        val inflater = android.view.LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view, onValidate, onReject, onView)
    }

    override fun getItemCount(): Int = reports.size

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }
}

// ViewHolder
class ReportViewHolder(
    itemView: android.view.View,
    val onValidate: (Report) -> Unit,
    val onReject: (Report) -> Unit,
    val onView: (Report) -> Unit
) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    private val textTitle: TextView = itemView.findViewById(R.id.textTitle)
    private val textTime: TextView = itemView.findViewById(R.id.textTime)
    private val textLocation: TextView = itemView.findViewById(R.id.textLocation)
    private val textDescription: TextView = itemView.findViewById(R.id.textDescription)
    private val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
    private val btnValidate: Button = itemView.findViewById(R.id.btnValidate)
    private val btnReject: Button = itemView.findViewById(R.id.btnReject)
    private val btnView: Button = itemView.findViewById(R.id.btnView)

    fun bind(report: Report) {
        textTitle.text = "Traffic Accident"
        textLocation.text = report.location
        textDescription.text = report.description
        textTime.text = report.timestamp
        imgIcon.setImageResource(R.drawable.ic_accident_report)

        btnValidate.setOnClickListener { onValidate(report) }
        btnReject.setOnClickListener { onReject(report) }
        btnView.setOnClickListener { onView(report) }
    }
}