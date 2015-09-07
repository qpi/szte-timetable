package etr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Timetable {

	private String cookie = "";

	private String semester = "";

	private String userAgent = "Mozilla/5.0 (Windows NT 6.1; rv:18.0) Gecko/20100101 Firefox/18.0";

	private String console = "";

	private boolean success = false;

	ArrayList<JSONObject> classes = new ArrayList<JSONObject>();

	String loginUrl = "https://web9.etr.u-szeged.hu/etr/Login.aspx";

	String calendarUrl = "https://web9.etr.u-szeged.hu/etr/Orarend/OrarendReload/?tipus=Hallgatoi&bontas=-1";

	BufferedWriter out;

	private void obtainCookie() throws IOException {
		URL url = new URL(loginUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setUseCaches(true);
		con.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		con.setRequestProperty("Accept-Language",
				"hu-hu,hu;q=0.8,en-US;q=0.5,en;q=0.3");
		con.setRequestProperty("Accept-Encoding", "gzip, deflate");
		con.setRequestProperty("User-Agent", userAgent);
		con.setRequestProperty("charset", "UTF-8");
		con.connect();
		setCookie(con.getHeaderField("Set-Cookie").split(";")[0]);
	}

	private boolean login(String username, String password) throws Exception {
		if (username.length() == 0 || password.length() == 0)
			return false;
		obtainCookie();
		URL url = new URL(loginUrl);

		String urlParameters = "username=" + username + "&" + "password="
				+ password + "&X-Requested-With=XMLHttpRequest";

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		con.setRequestProperty("DNT", "1");
		con.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		con.setRequestProperty("Accept-Language",
				"hu-hu,hu;q=0.8,en-US;q=0.5,en;q=0.3");
		con.setRequestProperty("Accept-Encoding", "gzip, deflate");
		con.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		con.setRequestProperty("User-Agent", userAgent);
		con.setRequestProperty("charset", "UTF-8");
		con.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		con.setRequestProperty("Cookie", getCookie());
		con.setRequestProperty("Content-Length",
				"" + Integer.toString(urlParameters.getBytes().length));
		con.setUseCaches(true);
		con.connect();

		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream(),
				"UTF-8");
		out.write(urlParameters);
		out.flush();
		out.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream(), "UTF-8"));
		String line = null;
		boolean success = true;
		while ((line = in.readLine()) != null) {
			if (line.contains("validation-summary-errors"))
				success = false;
		}

		return success;
	}

	private void getTimetable() throws Exception {

		String data = "";

		URL url = new URL(calendarUrl + "&ciklus=" + getSemester());

		URLConnection con = url.openConnection();
		con.setDoOutput(true);
		con.setRequestProperty("Cookie", getCookie());
		con.setRequestProperty("DNT", "1");
		con.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		con.setRequestProperty("Accept-Language",
				"hu-hu,hu;q=0.8,en-US;q=0.5,en;q=0.3");
		con.setRequestProperty("Accept-Encoding", "gzip, deflate");
		con.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		con.setRequestProperty("User-Agent", userAgent);
		con.setRequestProperty("charset", "UTF-8");
		con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
		con.connect();

		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream(),
				"UTF-8");
		out.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream(), "UTF-8"));
		String line = null;
		while ((line = in.readLine()) != null) {
			if (line.contains("businessHours:")) {
				data = line.replaceAll("^.*data: ", "");
				data = data.substring(0, (data.indexOf("}]") + 2))
						.replace("\\u003e", ">").replace("\\u003c", "<");
			}
		}
		JSONArray JSONdata = new JSONArray(data);
		if (JSONdata != null) {
			int len = JSONdata.length();
			for (int i = 0; i < len; i++) {
				classes.add(new JSONObject(JSONdata.get(i).toString()));
			}
		}
	}

	private void generateCSV() throws JSONException, IOException {

		StringBuilder content = new StringBuilder(
				"Start Date,End Date,Start Time,End Time,Subject,Location,All Day\n");
		for (JSONObject JSONclass : classes) {
			String start = (String) JSONclass.get("start");
			String end = (String) JSONclass.get("end");
			String name = (String) JSONclass.get("title");

			String startTime = start.split(" ")[1];

			String endTime = end.split(" ")[1];

			String description = (String) JSONclass.get("ToolTip");

			String code = description.substring((description
					.indexOf("Órarendi kód:</td><td>") + 22));
			code = code.substring(0, (code.indexOf("</td>"))).replace(" ", "");

			name = "(" + code + ") " + name;

			int startIndex = description.indexOf("pontok:</td><td>") + 16;
			int endIndex = description.indexOf("</td></tr></tbody></table>");

			String[] dates = description.substring(startIndex, endIndex)
					.replace(" ", "").split(",");

			for (int i = 0; i < dates.length; i++) {
				if (dates[i].contains(":"))
					dates[i] = dates[i].split(":")[1];
			}

			String year = "";

			for (int i = 0; i < dates.length; i++) {
				String month;
				String day;
				try {
					if (dates[i].length() > 6) {
						year = dates[i].substring(2, 4);
						month = dates[i].substring(5, 7);
						day = dates[i].substring(8, 10);
					} else {
						month = dates[i].substring(0, 2);
						day = dates[i].substring(3, 5);
					}

					// add start date
					content.append(month).append("/")
							.append(day)
							.append("/")
							.append(year)
							.append(",")
							// add end date
							.append(month).append("/")
							.append(day)
							.append("/")
							.append(year)
							.append(",")
							// add start time
							.append(startTime)
							.append(",")
							// add end time
							.append(endTime).append(",\"")
							.append(name.split("<br />")[0]).append("\",\"")
							.append(name.split("<br />")[1])
							.append("\",False\n");

				} catch (Exception e) {
					addToConsole("Nem lehet feldogozni a dátumot:"
							+ description.substring(startIndex, endIndex));
				}
			}
		}
		out.write(content.toString());
		out.close();
	}

	public Timetable(String username, String password, String semester) {
		try {
			out = new BufferedWriter(new FileWriter("timetable.csv"));
			if (login(username, password)) {
				addToConsole("Sikeres belépés!");
				setSemester(semester);
				getTimetable();
				generateCSV();
				setSuccess(true);
				addToConsole("A fájl sikeresen létrehozva!");
			} else {
				addToConsole("Hibás felhasználói név vagy jelszó!");
			}
		} catch (Exception e) {
			addToConsole("Váratlan hiba történt! " + e.getMessage());
		}
		System.out.println(getConsole());
	}
	public static void main(String[] args) throws Exception {

		new Timetable(args[0], args[1], args[2]);
	}

	public String getSemester() {
		return semester;
	}

	public void setSemester(String semester) {
		this.semester = semester;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public String getConsole() {
		return console;
	}

	private void addToConsole(String line) {
		this.console += line + "\n";
	}

	public boolean isSuccess() {
		return success;
	}

	private void setSuccess(boolean success) {
		this.success = success;
	}

}
