package com.android.ifarm.ifarming.ui.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.android.ifarm.ifarming.R;
import com.android.ifarm.ifarming.app.AppConfig;
import com.android.ifarm.ifarming.ui.db.DicFarm;
import com.android.ifarm.ifarming.ui.db.DicSars;
import com.android.ifarm.ifarming.ui.event.AddFarmEvent;
import com.android.ifarm.ifarming.ui.event.FarmEvent;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.OnClick;

public class SarsFragment extends BaseFragment {

    public static SarsFragment newFragment() {
        SarsFragment fragment = new SarsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sars, container, false);
        bindView(this, view);
        return view;
    }

    ArrayAdapter adapterFrom, adapterType, adapterPz;
    String sFrom, sType, sPz, sScanResult;
    List<DicFarm> farms;
    ArrayList<String> mData;
    long sCode, sTime;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //        farms = new Select().from(DicFarm.class).where("DicUid = ?", AppConfig.getUserId()).execute();//根据用户id搜索
        farms = new Select().from(DicFarm.class).execute();//搜索全部
        mData = new ArrayList<>();
        for (DicFarm farm : farms) {
            mData.add(farm.dicName);
        }
        adapterFrom = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mData);
        adapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFrom.setAdapter(adapterFrom);
        mFrom.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                sFrom = adapterFrom.getItem(arg2).toString();
                sCode = farms.get(arg2).dicCode;
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        adapterType = ArrayAdapter.createFromResource(getActivity(), R.array.type, android.R.layout.simple_spinner_item);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mType.setAdapter(adapterType);
        mType.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                sType = adapterType.getItem(arg2).toString();
                if (adapterType.getItem(arg2).toString().equals("牛")) {
                    adapterPz = ArrayAdapter.createFromResource(getActivity(), R.array.niu, android.R.layout.simple_spinner_item);
                } else if (adapterType.getItem(arg2).toString().equals("羊")) {
                    adapterPz = ArrayAdapter.createFromResource(getActivity(), R.array.yang, android.R.layout.simple_spinner_item);
                } else if (adapterType.getItem(arg2).toString().equals("猪")) {
                    adapterPz = ArrayAdapter.createFromResource(getActivity(), R.array.zhu, android.R.layout.simple_spinner_item);
                } else if (adapterType.getItem(arg2).toString().equals("鸡")) {
                    adapterPz = ArrayAdapter.createFromResource(getActivity(), R.array.ji, android.R.layout.simple_spinner_item);
                }
                adapterPz.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mPz.setAdapter(adapterPz);
                mPz.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        sPz = adapterPz.getItem(arg2).toString();
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    @Bind(R.id.from)
    AppCompatSpinner mFrom;
    @Bind(R.id.type)
    AppCompatSpinner mType;
    @Bind(R.id.pinzhong)
    AppCompatSpinner mPz;
    @Bind(R.id.num)
    TextView mNum;
    @Bind(R.id.time)
    TextView mTime;

    @OnClick(R.id.read)
    void onRead() {
        Intent openCameraIntent = new Intent(getActivity(), CaptureActivity.class);
        startActivityForResult(openCameraIntent, 0);
    }

    @OnClick(R.id.photo)
    void onPhoto() {

    }

    @OnClick(R.id.save)
    void onSave() {
        if (farms.size() == 0) {
            Snackbar.make(mFrom, "暂时还没有添加养殖场信息！", Snackbar.LENGTH_SHORT).setAction("现在去添加", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postEvent(new AddFarmEvent());
                }
            }).show();
            return;
        }
        if (TextUtils.isEmpty(sScanResult)) {
            Snackbar.make(mNum, "请扫描个体编号！", Snackbar.LENGTH_SHORT).setAction("现在去添加", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            }).show();
            return;
        }
        if (sTime == 0) {
            Snackbar.make(mTime, "发病时间不能为空！", Snackbar.LENGTH_SHORT).setAction("现在去添加", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            }).show();
            return;
        }
        DicSars sars = new DicSars(sFrom, sCode, sType, sPz, sScanResult, sTime, "", AppConfig.getUserId());
        sars.save();
        mNum.setText("");
        mTime.setText("");
        Toast.makeText(getActivity(), "保存成功！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerEventBus();
        Bundle args = getArguments();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterEventBus();
    }

    @Subscribe
    public void onEvent(FarmEvent event) {
        super.onEvent(event);
        //        farms = new Select().from(DicFarm.class).where("DicUid = ?", AppConfig.getUserId()).execute();//根据用户id搜索
        farms = new Select().from(DicFarm.class).execute();//搜索全部
        mData.clear();
        for (DicFarm farm : farms) {
            mData.add(farm.dicName);
        }
        adapterFrom.notifyDataSetChanged();
    }

    @OnClick(R.id.time)
    void editDate() {
        if (sTime == 0) {
            sTime = System.currentTimeMillis();
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(sTime);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerView datePickerView = new DatePickerView(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(sTime);
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, monthOfYear);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                final long time = c.getTimeInMillis();
                new TimePickerView(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(time);
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute);
                        sTime = c.getTimeInMillis();
                        updateTime();
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), DateFormat.is24HourFormat(getActivity())).show();
            }
        }, year, month, day);
        datePickerView.show();
    }

    private void updateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(sTime);
        mTime.setText(dateFormat.format(calendar.getTime()));
    }

    public class TimePickerView extends TimePickerDialog {

        public TimePickerView(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
            super(context, callBack, hourOfDay, minute, is24HourView);

        }

        @Override
        protected void onStop() {
//            super.onStop();
        }
    }

    public class DatePickerView extends DatePickerDialog {


        public DatePickerView(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
            super(context, callBack, year, monthOfYear, dayOfMonth);
        }

        @Override
        protected void onStop() {
//            super.onStop();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            Bundle bundle = data.getExtras();
            sScanResult = bundle.getString("result");
            mNum.setText(sScanResult);
        }
    }
}
