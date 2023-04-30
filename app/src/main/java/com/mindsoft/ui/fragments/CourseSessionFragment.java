package com.mindsoft.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mindsoft.R;
import com.mindsoft.data.model.Course;
import com.mindsoft.data.model.CourseAttendance;
import com.mindsoft.data.model.CourseSession;
import com.mindsoft.data.model.Role;
import com.mindsoft.data.model.Student;
import com.mindsoft.data.model.StudentAttended;
import com.mindsoft.data.model.User;
import com.mindsoft.data.model.UserLocation;
import com.mindsoft.data.repoistory.CourseRepository;
import com.mindsoft.databinding.FragmentCourseSessionBinding;
import com.mindsoft.ui.activity.DetectorActivity;
import com.mindsoft.ui.adapters.StudentAttendanceAdapter;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CourseSessionFragment extends Fragment {
    public static final int ATTENDANCE_TIMER = 5; // In Minutes
    private static final int REQUEST_WRITE_STORAGE = 1;
    private FragmentCourseSessionBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore db;
    private NavController mNavController;

    private boolean isAlreadyAttended = false;
    private boolean isTakingAttendance = false;
    private boolean isAttending = false;

    private CourseAttendance attendance;

    ActivityResultLauncher<Intent> launcher;
    private CourseSession session;
    private Course course;
    private StudentAttendanceAdapter adapter;
    private User instructor;

    private DatabaseReference currentUserLocation;
    private DatabaseReference instructorUserLocation;
    private ValueEventListener currentLocationListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCourseSessionBinding.inflate(inflater, container, false);


        assert getArguments() != null;

        mNavController = NavHostFragment.findNavController(this);

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));

        adapter = new StudentAttendanceAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        String courseId = getArguments().getString("courseId");
        String sessionId = getArguments().getString("sessionId");

        db = FirebaseFirestore.getInstance();

        db.collection(Course.COLLECTION).document(courseId).get().addOnSuccessListener(command -> {
            course = command.toObject(Course.class);
            db.collection(CourseSession.COLLECTION).document(sessionId).get().addOnSuccessListener(task -> {
                session = task.toObject(CourseSession.class);
                assert session != null;

                FirebaseDatabase database = FirebaseDatabase.getInstance();

                assert course != null;
                CourseRepository.getInstance().getEnrolledUsers(course).observe(requireActivity(), students -> {
                    database.getReference("course_attendance").child(sessionId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                attendance = snapshot.getValue(CourseAttendance.class);
                                assert attendance != null;
                            } else {
                                attendance = new CourseAttendance();
                                attendance.setCourseId(courseId);
                                attendance.setSessionId(sessionId);
                                attendance.setTaking(false);
                                students.forEach(user -> {
                                    attendance.getAttended().put(user.getId(), false);
                                });
                                database.getReference("course_attendance").child(sessionId).setValue(attendance);
                            }
                            currentUserLocation = database.getReference("user_location").child(User.current.getId());
                            currentLocationListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        currentUserLocation.removeValue();
                                    }
                                    User.current.fetchLocation(requireActivity());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            };

                            if (attendance.getAttended().containsKey(User.current.getId()) && attendance.getAttended().get(User.current.getId())) {
                                isAlreadyAttended = true;
                                binding.attend.setVisibility(View.GONE);
                            }

                            if (isAlreadyAttended) {
                                currentUserLocation.removeEventListener(currentLocationListener);
                            }

                            isTakingAttendance = attendance.isTaking();
                            if (User.current.hasRole(Role.STUDENT)) {
                                binding.attend.setVisibility(View.VISIBLE);
                                binding.attend.setOnClickListener(v -> {
                                    binding.attend.setAlpha(0.5f);
                                    binding.attend.setEnabled(false);
                                    attend(course, session);
                                });
                            }
                            if (isTakingAttendance) {
                                binding.attend.setAlpha(1f);
                                binding.attend.setEnabled(true);
                                currentUserLocation.addValueEventListener(currentLocationListener);
                            } else {
                                binding.attend.setAlpha(0.5f);
                                binding.attend.setEnabled(false);
                                currentUserLocation.removeEventListener(currentLocationListener);
                            }

                            List<StudentAttended> attendance_ = students.stream().map(student -> {
                                StudentAttended studentAttended = new StudentAttended();
                                studentAttended.setId(student.getId());
                                studentAttended.setFullName(student.getFullName());
                                db.collection(Student.COLLECTION).document(student.getId()).get().addOnSuccessListener(documentSnapshot -> {
                                    Student std = documentSnapshot.toObject(Student.class);
                                    if (std != null) {
                                        studentAttended.setSection(std.getSection());
                                    }
                                });
                                if (attendance.getAttended().containsKey(student.getId())) {
                                    studentAttended.setAttended(attendance.getAttended().get(student.getId()));
                                }
                                return studentAttended;
                            }).collect(Collectors.toList());


                            adapter.setStudents(attendance_);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                });

                session.getInstructor().get().addOnSuccessListener(userObj -> {
                    instructor = userObj.toObject(User.class);
                    binding.courseName.setText(course.getName());
                    binding.instructorName.setText("By " + instructor.getFullName());
                    binding.sessionTitle.setText(session.getTitle());
                    Handler handler = new Handler();


                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Timestamp start = session.getDate();
                            Timestamp now = Timestamp.now();
                            long estimated = now.toDate().getTime() - start.toDate().getTime();
                            int seconds = (int) (estimated / 1000) % 60;
                            int minutes = (int) (estimated / 1000 / 60) % 60;
                            int hours = (int) (estimated / 1000 / 60 / 60) % 24;
                            binding.time.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                            handler.postDelayed(this, 1000);
                        }
                    };

                    if (session.getStatusValue() == CourseSession.Status.ONGOING) {
                        handler.post(runnable);
                    } else {
                        binding.time.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(session.getDate().toDate()));
                    }

                    if (instructor.getId().equals(User.current.getId())) {
                        if (session.getStatusValue() == CourseSession.Status.ONGOING) {
                            binding.close.setVisibility(View.VISIBLE);
                            binding.close.setOnClickListener(v -> {
                                session.setStatus(1);
                                session.getReference().set(session).addOnSuccessListener(unused -> {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("courseId", courseId);
                                    mNavController.navigate(R.id.action_session_to_course, bundle);
                                });
                            });

                            binding.takeAttendance.setVisibility(View.VISIBLE);
                            binding.takeAttendance.setOnClickListener(v -> {
                                startAttendance(course, session);
                                binding.takeAttendance.setAlpha(0.5f);
                                binding.takeAttendance.setEnabled(false);
                            });
                        } else {
                            binding.export.setVisibility(View.VISIBLE);
                            binding.export.setOnClickListener(v -> {
                                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
                                } else {
                                    export(course, session);
                                }
                            });
                        }
                    }

                });
            });

        });

        return binding.getRoot();
    }

    private void export(Course course, CourseSession session) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Map<Integer, Map<String, Boolean>> data = new HashMap<>();

        XSSFCellStyle bordered = workbook.createCellStyle();
        bordered.setBorderBottom(BorderStyle.MEDIUM);
        bordered.setBorderTop(BorderStyle.MEDIUM);
        bordered.setBorderRight(BorderStyle.MEDIUM);
        bordered.setBorderLeft(BorderStyle.MEDIUM);

        bordered.setAlignment(HorizontalAlignment.LEFT);
        bordered.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont font = workbook.createFont();
        font.setBold(true);

        XSSFCellStyle headStyle = workbook.createCellStyle();
        headStyle.setFont(font);

        headStyle.setBorderBottom(BorderStyle.MEDIUM);
        headStyle.setBorderTop(BorderStyle.MEDIUM);
        headStyle.setBorderRight(BorderStyle.MEDIUM);
        headStyle.setBorderLeft(BorderStyle.MEDIUM);

        XSSFCellStyle detailsHeadStyle = workbook.createCellStyle();
        detailsHeadStyle.setFont(font);

        detailsHeadStyle.setBorderBottom(BorderStyle.MEDIUM);
        detailsHeadStyle.setBorderTop(BorderStyle.MEDIUM);
        detailsHeadStyle.setBorderRight(BorderStyle.MEDIUM);
        detailsHeadStyle.setBorderLeft(BorderStyle.MEDIUM);

        XSSFCellStyle detailStyle = workbook.createCellStyle();
        detailStyle.setAlignment(HorizontalAlignment.CENTER);
        detailStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        detailStyle.setBorderBottom(BorderStyle.MEDIUM);
        detailStyle.setBorderTop(BorderStyle.MEDIUM);
        detailStyle.setBorderRight(BorderStyle.MEDIUM);
        detailStyle.setBorderLeft(BorderStyle.MEDIUM);


        FirebaseDatabase db = FirebaseDatabase.getInstance();
        for (StudentAttended student : adapter.getStudents()) {
            if (!data.containsKey(student.getSection())) {
                data.put(student.getSection(), new HashMap<>());
            }
            data.get(student.getSection()).put(student.getFullName(), attendance.getAttended().getOrDefault(student.getId(), false));
        }

        Map<Integer, XSSFSheet> sheets = new HashMap<>();
        for (Integer section : data.keySet()) {
            XSSFSheet sheet = workbook.createSheet("Section " + section);
            XSSFRow detailsHeading = sheet.createRow(0);

            XSSFCell instructorNameCol = detailsHeading.createCell(0);

            XSSFCell courseCol = detailsHeading.createCell(1);
            XSSFCell dateCol = detailsHeading.createCell(2);
            instructorNameCol.setCellStyle(detailsHeadStyle);
            instructorNameCol.setCellValue("Instructor");
            dateCol.setCellValue("Date");
            dateCol.setCellStyle(detailsHeadStyle);
            courseCol.setCellValue("Course");
            courseCol.setCellStyle(detailsHeadStyle);

            XSSFRow details = sheet.createRow(1);
            sheet.setDefaultColumnWidth(30);
            XSSFCell instructorCell = details.createCell(0);
            XSSFCell courseCell = details.createCell(1);
            XSSFCell dateCell = details.createCell(2);
            instructorCell.setCellValue(this.instructor.getFullName());
            courseCell.setCellValue(course.getName());
            dateCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(session.getDate().toDate()));

            instructorCell.setCellStyle(detailStyle);
            courseCell.setCellStyle(detailStyle);
            dateCell.setCellStyle(detailStyle);

            XSSFRow heading = sheet.createRow(3);
            XSSFCell num = heading.createCell(0);
            XSSFCell name = heading.createCell(1);
            XSSFCell attended = heading.createCell(2);
            num.setCellValue("#");
            name.setCellValue("Name");
            attended.setCellValue("Attended");
            num.setCellStyle(headStyle);
            name.setCellStyle(headStyle);
            attended.setCellStyle(headStyle);
            int i = 1;
            for (Map.Entry<String, Boolean> info : Objects.requireNonNull(data.get(section)).entrySet()) {
                XSSFRow studentRow = sheet.createRow(3 + i);
                XSSFCell n = studentRow.createCell(0);
                n.setCellValue(i);
                n.setCellStyle(bordered);
                XSSFCell stdName = studentRow.createCell(1);
                stdName.setCellValue(info.getKey());
                stdName.setCellStyle(bordered);
                XSSFCell stdAttended = studentRow.createCell(2);
                stdAttended.setCellValue(info.getValue() ? "T" : "F");
                stdAttended.setCellStyle(bordered);

                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadsDir, String.format("%s - %s.xlsx", course.getName(), session.getTitle()));
                try {
                    // Write some data to the file
                    FileOutputStream outputStream = new FileOutputStream(file);
                    workbook.write(outputStream);
                    // Notify the media scanner to add the file to the Downloads directory
                    MediaScannerConnection.scanFile(requireContext(), new String[]{file.getPath()}, null, null);

                    // Show a toast message to indicate that the file was saved in the Downloads directory
                    Toast.makeText(requireActivity(), "File saved in Downloads directory", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void attend(Course course, CourseSession session) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference ref1 = database.getReference("user_location").child(session.getInstructor().getId());
        DatabaseReference ref2 = database.getReference("user_location").child(User.current.getId());

        final UserLocation[] instructorLocation = new UserLocation[1];
        final UserLocation[] myLocation = new UserLocation[1];

        ref1.get().addOnSuccessListener(snapshot -> {
            if (!isTakingAttendance || isAlreadyAttended) {
                Toast.makeText(requireActivity(), "The instructor is no longer taking attendance.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshot.exists()) {
                instructorLocation[0] = snapshot.getValue(UserLocation.class);
                ref2.get().addOnSuccessListener(snapshot2 -> {
                    if (snapshot2.exists() && !isAttending) {
                        myLocation[0] = snapshot2.getValue(UserLocation.class);
                        if (myLocation[0] == null || instructorLocation[0] == null) return;

                        System.out.println("Distance between " + myLocation[0].getLocation().distanceTo(instructorLocation[0].getLocation()));

                        isAttending = true;

                        Intent intent = new Intent(requireActivity(), DetectorActivity.class);
                        intent.putExtra("train_mode", false);
                        intent.putExtra("title", "Scan your face to take attendance");
                        DetectorActivity.onComplete = (success, context) -> {
                            System.out.println(".. " + success);
                            if (success) {
                                database.getReference("course_attendance").child(attendance.getSessionId()).get().addOnSuccessListener(dataSnapshot -> {
                                    if (dataSnapshot.exists()) {
                                        CourseAttendance attendance = dataSnapshot.getValue(CourseAttendance.class);
                                        assert attendance != null;
                                        attendance.getAttended().put(User.current.getId(), true);
                                        database.getReference("course_attendance").child(attendance.getSessionId()).setValue(attendance);
                                        binding.attend.setVisibility(View.GONE);
                                    }
                                });
                                context.finish();
                            }
                        };
                        startActivity(intent);
                    }
                });
            } else {
                binding.attend.setAlpha(1f);
                binding.attend.setEnabled(true);
                Toast.makeText(requireActivity(), "Cannot determine instructor's location.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (currentUserLocation != null && currentLocationListener != null) {
            currentUserLocation.removeEventListener(currentLocationListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (course != null && attendance != null) {
            if (attendance.isTaking()) {
                if (User.current.getId().equals(course.getProfessor().getId()) || course.getTeachingAssistantsIds().contains(User.current.getId())) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    attendance.setTaking(false);
                    database.getReference("course_attendance").child(session.getId()).setValue(attendance);
                }
            }
        }

    }

    private void startAttendance(Course course, CourseSession session) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        assert attendance != null;
        attendance.setTaking(true);
        database.getReference("course_attendance").child(session.getId()).setValue(attendance);
        binding.timer.setVisibility(View.VISIBLE);

        currentUserLocation = database.getReference("user_location").child(User.current.getId());
        currentLocationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserLocation.removeValue();
                }
                User.current.fetchLocation(requireActivity());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        currentUserLocation.addValueEventListener(currentLocationListener);

        CountDownTimer timer = new CountDownTimer(1000 * 60 * ATTENDANCE_TIMER, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = (int) (millisUntilFinished / 1000 / 60);
                binding.timer.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                binding.timer.setVisibility(View.GONE);
                binding.takeAttendance.setAlpha(1f);
                binding.takeAttendance.setEnabled(true);
                attendance.setTaking(false);
                database.getReference("course_attendance").child(session.getId()).setValue(attendance);
                currentUserLocation.removeEventListener(currentLocationListener);
            }
        };


        timer.start();
    }
}
