import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;

public class InternTaskMentor {
    static String internName = "Intern", internEmail = "";
    static String taskTitle = "Build a simple login page";
    static String taskDetails = "Create a responsive login page with email and password fields, validation, and a clear submit button.";
    static String expected = "html,css,validation,responsive,login";
    static String reviewDate = LocalDate.now().plusDays(2).toString();
    static int attempts = 0;
    static String latestFeedback = "Assign a task to begin.";
    static String status = "Waiting for task setup";
    static final List<String> history = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8081), 0);
        server.createContext("/", InternTaskMentor::home);
        server.createContext("/assign", InternTaskMentor::assign);
        server.createContext("/submit", InternTaskMentor::submit);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Intern Task Mentor running at http://127.0.0.1:8080");
    }

    static void home(HttpExchange ex) throws IOException {
        StringBuilder h = new StringBuilder();
        h.append("""
        <!doctype html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Intern Task Mentor</title><style>
        *{box-sizing:border-box}body{margin:0;font-family:Arial,sans-serif;background:#f3f6fb;color:#172033}
        header{background:#182b4d;color:white;padding:24px 7%}header h1{margin:0 0 6px}
        main{max-width:1050px;margin:24px auto;padding:0 16px}.grid{display:grid;grid-template-columns:1fr 1fr;gap:18px}
        .card{background:white;padding:22px;border-radius:14px;box-shadow:0 5px 20px #1c315014}
        label{display:block;font-weight:bold;margin:12px 0 6px}input,textarea{width:100%;padding:11px;border:1px solid #cbd5e1;border-radius:8px;font:inherit}
        textarea{min-height:90px}button{background:#2457d6;color:white;border:0;padding:12px 17px;border-radius:8px;font-weight:bold;cursor:pointer;margin-top:14px}
        .muted{color:#637087;font-size:14px}.pill{display:inline-block;background:#e5edff;padding:6px 10px;border-radius:99px;font-size:13px}
        .lang{border-top:1px solid #e7ebf2;padding-top:10px;margin-top:10px}.link{overflow-wrap:anywhere;color:#174bb5}
        @media(max-width:760px){.grid{grid-template-columns:1fr}header{padding:20px}}
        </style></head><body><header><h1>Intern Task Mentor</h1><div>Assign • Explain • Review • Improve</div></header><main><div class="grid">
        <section class="card"><h2>1. Assign a task</h2><form method="post" action="/assign">
        <label>Intern name</label><input name="name" required placeholder="Enter intern name">
        <label>Intern email</label><input name="email" type="email" placeholder="intern@example.com">
        <label>Task title</label><input name="title" required value="Build a simple login page">
        <label>Task instructions</label><textarea name="details" required>Create a responsive login page with email and password fields, validation, and a clear submit button.</textarea>
        <label>Expected-output keywords (comma-separated)</label><input name="expected" value="HTML, CSS, validation, responsive, login">
        <label>Review date</label><input name="date" type="date" required>
        <button type="submit">Assign task</button></form></section>
        """);
        h.append("<section class=\"card\"><h2>2. Task status</h2><span class=\"pill\">").append(esc(status)).append("</span>");
        h.append("<p><b>Intern:</b> ").append(esc(internName)).append("</p><p><b>Task:</b> ").append(esc(taskTitle)).append("</p>");
        h.append("<p><b>Review date:</b> ").append(esc(reviewDate)).append("</p>");
        h.append("<p><b>Submission link:</b> <a class=\"link\" href=\"/submit\">http://127.0.0.1:8080/submit</a></p>");
        h.append("<h3>Task explanation</h3><div class=\"lang\"><b>English</b><p>").append(esc(taskDetails)).append("</p></div>");
        h.append("<div class=\"lang\"><b>Tamil</b><p>Indha task-ai instructions-padi complete panni review date-kulla submit pannunga.</p></div>");
        h.append("<div class=\"lang\"><b>Hindi</b><p>दिए गए निर्देशों के अनुसार काम पूरा करें और समीक्षा की तारीख तक जमा करें।</p></div>");
        h.append("""
        <label>Listen to explanation</label><select id="language"><option value="en-US">English</option><option value="ta-IN">Tamil</option><option value="hi-IN">Hindi</option></select>
        <button onclick="speak()">🔊 Play explanation</button><p class="muted">Browser voice availability depends on installed voices.</p>
        <h3>Latest feedback</h3><p>
        """).append(esc(latestFeedback)).append("</p><p><b>Iteration:</b> ").append(attempts).append(" &nbsp; <b>Status:</b> ").append(esc(status)).append("</p>");
        h.append("<h3>Email preview (not sent)</h3><p>To: ").append(esc(internEmail.isBlank() ? "(enter an email)" : internEmail));
        h.append("</p><p>Subject: Task assigned — ").append(esc(taskTitle)).append("</p><p>Submit here: http://127.0.0.1:8080/submit<br>Review date: ").append(esc(reviewDate)).append("</p></section></div>");
        h.append("<section class=\"card\" style=\"margin-top:18px\"><h2>3. Submit your work</h2><p>Paste your work or a summary and get demo feedback against expected keywords.</p><a href=\"/submit\"><button>Open submission page</button></a><h3>Review history</h3><ul>").append(historyHtml()).append("</ul>");
        h.append("<p class=\"muted\">Prototype notice: keyword matching is not real AI evaluation. Email is only a preview and is not sent. This local demo is visible only on this computer.</p></section></main>");
        h.append("""
        <script>
        function speak(){let l=document.getElementById('language').value;let t=l==='ta-IN'?'Indha task-ai instructions-padi complete panni review date-kulla submit pannunga.':l==='hi-IN'?'दिए गए निर्देशों के अनुसार काम पूरा करें और समीक्षा की तारीख तक जमा करें।':document.querySelector('.lang p').innerText;let u=new SpeechSynthesisUtterance(t);u.lang=l;speechSynthesis.cancel();speechSynthesis.speak(u);}
        </script></body></html>
        """);
        send(ex, 200, h.toString());
    }

    static void assign(HttpExchange ex) throws IOException {
        Map<String,String> f = form(ex);
        internName = f.getOrDefault("name","Intern");
        internEmail = f.getOrDefault("email","");
        taskTitle = f.getOrDefault("title","Assigned task");
        taskDetails = f.getOrDefault("details","");
        expected = f.getOrDefault("expected","").toLowerCase(Locale.ROOT);
        reviewDate = f.getOrDefault("date",LocalDate.now().plusDays(2).toString());
        attempts = 0; latestFeedback = "Task assigned. Submit your work using the submission page.";
        status = "Task assigned";
        history.clear(); history.add("Task assigned on " + LocalDate.now() + "; review date: " + reviewDate);
        redirect(ex, "/");
    }

    static void submit(HttpExchange ex) throws IOException {
        if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
            Map<String,String> f = form(ex);
            String work = f.getOrDefault("work","");
            attempts++;
            Set<String> keys = new LinkedHashSet<>();
            for (String k : expected.split("[,;\\n]")) if (!k.trim().isEmpty()) keys.add(k.trim().toLowerCase(Locale.ROOT));
            List<String> missing = new ArrayList<>();
            String lower = work.toLowerCase(Locale.ROOT);
            for (String k : keys) if (!lower.contains(k)) missing.add(k);
            if (keys.isEmpty() || missing.isEmpty()) {
                status = "Demo review: criteria keywords found";
                latestFeedback = "Expected keywords were found. Please manually check quality before marking complete.";
            } else {
                status = "Changes requested";
                latestFeedback = "Please improve your submission. Expected keywords not found: " + String.join(", ", missing) + ". Add evidence for each requirement, then resubmit.";
                reviewDate = LocalDate.now().plusDays(2).toString();
            }
            history.add("Attempt " + attempts + " — " + status + " — " + LocalDate.now());
            redirect(ex, "/"); return;
        }
        String body = """
        <!doctype html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Submit work</title>
        <style>body{font-family:Arial;background:#f3f6fb;margin:0;padding:24px;color:#172033}.card{max-width:760px;margin:auto;background:white;padding:24px;border-radius:14px}textarea{width:100%;min-height:260px;padding:12px;font:inherit;border:1px solid #cbd5e1;border-radius:8px}button{background:#2457d6;color:white;padding:12px 18px;border:0;border-radius:8px;margin-top:12px;cursor:pointer}a{color:#2457d6}</style></head><body><div class="card">
        <h1>Submit your task</h1>
        """;
        body += "<p><b>Task:</b> " + esc(taskTitle) + "</p><p><b>Review date:</b> " + esc(reviewDate) + "</p>";
        body += """
        <form method="post" action="/submit"><label for="work">Paste your work, solution summary, or evidence below:</label><br><br><textarea id="work" name="work" required placeholder="Describe your completed work and how it meets each expected criterion..."></textarea><br><button type="submit">Submit for review</button></form>
        <p><a href="/">← Back to dashboard</a></p><p style="color:#637087">This prototype accepts text only. It does not upload files or send email.</p></div></body></html>
        """;
        send(ex, 200, body);
    }

    static Map<String,String> form(HttpExchange ex) throws IOException {
        String raw = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String,String> map = new HashMap<>();
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=",2);
            map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return map;
    }
    static String historyHtml() {
        if (history.isEmpty()) return "<li>No reviews yet.</li>";
        StringBuilder b = new StringBuilder();
        for (int i=history.size()-1;i>=0;i--) b.append("<li>").append(esc(history.get(i))).append("</li>");
        return b.toString();
    }
    static String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");
    }
    static void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type","text/html; charset=UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os=ex.getResponseBody()) { os.write(bytes); }
    }
    static void redirect(HttpExchange ex, String path) throws IOException {
        ex.getResponseHeaders().set("Location",path);
        ex.sendResponseHeaders(303,-1); ex.close();
    }
}
