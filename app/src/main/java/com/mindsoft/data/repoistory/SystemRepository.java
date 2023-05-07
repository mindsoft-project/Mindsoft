package com.mindsoft.data.repoistory;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mindsoft.data.model.Department;
import com.mindsoft.data.model.StudentInfo;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SystemRepository {

    private static final String BASE_URL = "http://193.227.0.21/Nctu/Registration/";

    private MutableLiveData<String> studentCode = new MutableLiveData<>();

    private MutableLiveData<StudentInfo> studentInfo = new MutableLiveData<>();

    private static final Map<String, Integer> departments = new HashMap<>() {{
        put("برنامج تكنولوجيا المعلومات", Department.ICT.getId());
    }};

    public LiveData<String> getStudentCode(String nationalID) {
        fetchStudentCode(nationalID);
        return studentCode;
    }

    public LiveData<StudentInfo> getStudentInfo(String studentCode, String nationalID) {
        fetchStudentData(studentCode, nationalID);
        return studentInfo;
    }

    public static Map<String, String> getFormFields(Document doc) {
        Map<String, String> result = new HashMap<>();
        String[] ids = {"__VIEWSTATE", "__EVENTVALIDATION", "__VIEWSTATEGENERATOR"};

        for (String id : ids) {
            Element element = doc.getElementById(id);
            if (element != null) {
                result.put(id, element.val());
            }
        }

        return result;
    }

    public void fetchStudentCode(String nationalID) {
        new Thread(() -> {
            try {
                Document doc = Jsoup.connect(BASE_URL + "ED" + "/GetStudCode.aspx").get();
                Map<String, String> fields = getFormFields(doc);
                fields.put("ctl00$ScriptManager1", "ctl00$cntphmaster$panal1|ctl00$cntphmaster$btnGet");
                fields.put("ctl00$cntphmaster$drpAsFacultyInfoId", "1");
                fields.put("ctl00$cntphmaster$txt_Nationalnum", nationalID);
                fields.put("ctl00$cntphmaster$btnGet", "كود الطالب");

                Connection req = Jsoup.connect(BASE_URL + "ED" + "/GetStudCode.aspx").header("Content-Type", "application/x-www-form-urlencoded");

                for (Map.Entry<String, String> entry : fields.entrySet()) {
                    req.data(entry.getKey(), entry.getValue());
                }

                doc = req.post();
                Element studentElement = doc.getElementById("ctl00_cntphmaster_lblStudCode");
                if (studentElement != null) {
                    String code = studentElement.text().split(":")[1].trim();
                    studentCode.postValue(code);
                } else {
                    studentCode.postValue(null);
                }
            } catch (IOException ignored) {
                ignored.printStackTrace();
                studentCode.postValue(null);
            }
        }).start();
    }

    public void fetchStudentData(String studentCode, String nationalID) {
        new Thread(() -> {
            try {
                Document doc = Jsoup.connect(BASE_URL + "ED_Login.aspx").get();
                Map<String, String> fields = getFormFields(doc);
                fields.put("ctl00$ScriptManager1", "ctl00$cntphmaster$panal1|ctl00$cntphmaster$btn_Login");
                fields.put("ctl00$cntphmaster$txt_StudCode", studentCode);
                fields.put("ctl00$cntphmaster$txt_Nationalnum", nationalID);
                fields.put("ctl00$cntphmaster$btn_Login", "تسجيل دخول");
                fields.put("__ASYNCPOST", "true");

                Connection req = Jsoup.connect(BASE_URL + "ED_Login.aspx").method(Connection.Method.POST).header("Content-Type", "application/x-www-form-urlencoded");

                for (Map.Entry<String, String> entry : fields.entrySet()) {
                    req.data(entry.getKey(), entry.getValue());
                }
                Connection.Response resp = req.execute();

                if (resp.body().contains("OR_MAIN_PAGE")) {
                    doc = Jsoup.connect(BASE_URL + "ED/OR_MAIN_PAGE.aspx").cookies(resp.cookies()).get();

                    Element studentName = doc.getElementById("ctl00_lblstudname");
                    Element phase = doc.getElementById("ctl00_lblphase");
                    Element sem = doc.getElementById("ctl00_lblsem");
                    Element program = doc.getElementById("ctl00_lblasnode");
                    Element mail = doc.getElementById("ctl00_lblStudMali");

                    if (studentName != null && phase != null && sem != null && program != null && mail != null) {
                        String[] name = studentName.text().replace("الاسم:", "").trim().split(" ");
                        String _phase = phase.text().trim();
                        String _sem = sem.text().trim();
                        String _dep = program.text().replace("القسم / الشعبة:", "").trim();
                        String _mail = mail.text().replace("البريد الجامعي: ", "").trim();

                        int year = _phase.equals("الفرقة الاولى") ? 1 : _phase.equals("الفرقة الثانية") ? 2 : _phase.equals("الفرقة الثالثة") ? 3 : _phase.equals("الفرقة الرابعة") ? 4 : 0;
                        int semester = _sem.contains("الثانى") ? 0 : 1;

                        StudentInfo info = new StudentInfo();
                        info.setStudentCode(studentCode);
                        info.setNationalID(nationalID);
                        info.setFirstName(name[0]);
                        info.setLastName(name[1] + " " + name[2]);
                        info.setSemester(2 * year - semester);
                        info.setEmail(_mail);
                        if (departments.containsKey(_dep)) {
                            info.setDepartmentID(departments.get(_dep));
                        }
                        studentInfo.postValue(info);
                    } else {
                        System.out.println("CKIER");
                        studentInfo.postValue(null);
                    }
                } else {
                    System.out.println("FREIASD");
                    studentInfo.postValue(null);
                }
            } catch (IOException ignored) {
                ignored.printStackTrace();
                System.out.println("AIOIJD");
                studentInfo.postValue(null);
            }
        }).start();
    }
}
