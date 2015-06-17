package com.dz.statuscheck;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class QueryStatus extends AsyncTask<String,Void,List<Response>>{

    public static final String URL_USCIS = "https://egov.uscis.gov/casestatus/mycasestatus.do";
    MainCheckActivity mainActivity;
    ProgressDialog pd;
    Dialog detailDialog;
    String detailToShow;
    public QueryStatus(MainCheckActivity mainCheckActivity) {
        this.mainActivity = mainCheckActivity;
    }

    public String checkStatus(String targetURL, String urlParameters) {
		HttpURLConnection connection = null;
		try {
			// Create connection
			URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			connection.setRequestProperty("Content-Length",
					Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); 
			String line;
			boolean startRecord = false;
			int lineToRecord = 3;
			while ((line = rd.readLine()) != null) {
				if(!startRecord && line.contains("rows text-center")){
					response.append(line.trim());
					response.append('\r');
					startRecord = true;
				}else if(startRecord && lineToRecord > 0){
					response.append(line.trim());
					response.append('\r');
					lineToRecord -= 1;
				}else if(lineToRecord <= 0){
					break;
				}
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	@Override
	protected List<Response> doInBackground(String... params) {
		List<Response> resList = new ArrayList<Response>();

		for(int i = 0; i < params.length; i+=2) {
			String res = checkStatus(URL_USCIS,"appReceiptNum="+params[i]);
			Response response = Parser.parse(res);
			response.setReceiptNum(params[i] + " - " + params[i+1]);
			resList.add(response);
		}
		return resList;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
        pd = new ProgressDialog(mainActivity);
        pd.setMessage("loading");
        pd.show();
    }

	@Override
	protected void onPostExecute(List<Response> result) {
		super.onPostExecute(result);
		if (pd != null)
		{
			pd.dismiss();
		}
        createContentOnScreen(result);
	}
    List<Response> resList;
    private void createContentOnScreen(List<Response> resList) {
        this.resList = resList;
        LinearLayout layout = (LinearLayout) mainActivity.findViewById(R.id.content_layout);
        detailDialog = new Dialog(mainActivity);
        detailDialog.setTitle("Detail");

        layout.removeAllViews();
        View.OnClickListener onclick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout contentLayout = (LinearLayout) mainActivity.findViewById(R.id.content_layout);
                int index = contentLayout.indexOfChild(v);
                TextView newTextView = new TextView(mainActivity);
                newTextView.setText(Html.fromHtml(QueryStatus.this.resList.get(index).getResDetail()));
                newTextView.setPadding(10,10,10,10);
                newTextView.setTextColor(Color.BLUE);
                detailDialog.setContentView(newTextView);
                detailDialog.show();
            }
        };
        LinearLayout newLayout;
        for(Response res : resList ){
            newLayout = new LinearLayout(mainActivity);
            newLayout.setOrientation(LinearLayout.VERTICAL);
            newLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            TextView tv = new TextView(mainActivity);
            tv.setText(res.getReceiptNum());
			tv.setTextColor(Color.BLUE);
            newLayout.addView(tv);
			tv = new TextView(mainActivity);
			tv.setText(res.getAppType() + "    " + res.getResTitle());
			tv.setTextColor(Color.BLUE);
            newLayout.addView(tv);
			tv = new TextView(mainActivity);
            String trimed = Html.fromHtml(res.getResDetail()).subSequence(0,50).toString();
			tv.setText(trimed+"...Click for details");
            tv.setPadding(0, 0, 0, 10);
            newLayout.addView(tv);
            newLayout.setOnClickListener(onclick);
            layout.addView(newLayout);
		}
    }
}
