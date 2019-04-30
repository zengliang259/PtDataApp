package com.pt.ptdataapp.Frame;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pt.ptdataapp.Model.DataManager;
import com.pt.ptdataapp.Model.PatientInfo;
import com.pt.ptdataapp.R;
import com.pt.ptdataapp.uiUtils.OnViewPagerListener;
import com.pt.ptdataapp.uiUtils.ViewPagerLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class MainPage extends Fragment {

    private View rootView;
    private static final String TAG = "MainPage";
    private RecyclerView mRecyclerView;
    private MainPage.MyAdapter mAdapter;
    private ViewPagerLayoutManager mLayoutManager;
    private int curSelectIndex = 0;
    private Context activityContext;
    private Handler mHandler;

    public MainPage()
    {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_main_page, container, false);
        }

        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        initView();
        return rootView;
    }

    public void SetContext(Context context)
    {
        activityContext = context;

    }

    public void NotifyListDataRefresh()
    {
        mHandler.sendEmptyMessage(1);
    }

    private void initView() {

        if (mLayoutManager == null)
        {
            mLayoutManager = new ViewPagerLayoutManager(activityContext, OrientationHelper.HORIZONTAL);
            initListener();
        }
        if (mAdapter == null)
        {
            mAdapter = new MainPage.MyAdapter(DataManager.getInstance().GetPatientList());
        }
        if (mRecyclerView == null)
        {
            mRecyclerView = rootView.findViewById(R.id.patientListView);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapter);
        }
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        if(mAdapter ==null){
                            mAdapter = new MainPage.MyAdapter(DataManager.getInstance().GetPatientList());
                            mRecyclerView.setAdapter(mAdapter);
                        }else{
                            mAdapter.notifyDataSetChanged();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void initListener(){
        mLayoutManager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            public void onInitComplete() {

            }

            @Override
            public void onPageRelease(boolean isNext,int position) {
                Log.e(TAG,"释放位置:"+position +" 下一页:"+isNext);

            }

            @Override
            public void onPageSelected(int position,boolean isEnd) {
                Log.e(TAG,"选中位置:"+position+"  是否是滑动到底部:"+isEnd);
                curSelectIndex = position;
            }


            public void onLayoutComplete() {
                // 开始
            }

        });
    }

    /**
     * 保存编辑数据
     */
    public void SaveEditData()
    {
        MainPage.MyAdapter.ViewHolder viewHolder = mAdapter.curShowViewHolder;
        List<String> printList = new ArrayList<>();
        printList.add(viewHolder.titleLabel.getText().toString());
        printList.add(viewHolder.IDLabel.getText().toString());
        printList.add(viewHolder.patientNameLabel.getText().toString());
        printList.add(viewHolder.resultLabel.getText().toString());
        printList.add(viewHolder.doctorNameLabel.getText().toString());
        printList.add(viewHolder.checkDateLabel.getText().toString());
        printList.add(viewHolder.reportDateLabel.getText().toString());
        DataManager.getInstance().SavePrintContentList(printList);
    }

    class MyAdapter extends RecyclerView.Adapter<MainPage.MyAdapter.ViewHolder> {
        private List<MainPage.MyAdapter.ViewHolder> itemViewList = new ArrayList<MainPage.MyAdapter.ViewHolder>();
        private List<PatientInfo> mAList;
        private MainPage.MyAdapter.ViewHolder curShowViewHolder;
        public MyAdapter(List<PatientInfo> list)
        {
            mAList = list;
        }


        @Override
        public MainPage.MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient_info, parent, false);
            MainPage.MyAdapter.ViewHolder viewHolder = new MainPage.MyAdapter.ViewHolder(view);
            itemViewList.add(viewHolder);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MainPage.MyAdapter.ViewHolder holder, int position) {
            PatientInfo pInfo = mAList.get(position);
            if (pInfo != null) {
                holder.titleLabel.setText(pInfo.title);
                holder.IDLabel.setText("ID:" + Integer.toString(pInfo.ID));
                holder.patientNameLabel.setText("病人姓名:" + pInfo.patientName);
                holder.resultLabel.setText("INR:" + pInfo.checkResult);
                holder.doctorNameLabel.setText("医生姓名" + pInfo.doctorName);
                holder.checkDateLabel.setText("检测日期" + pInfo.checkDate);
                holder.reportDateLabel.setText("报告日期" + pInfo.reportDate);
            }
            curShowViewHolder = holder;
        }

        @Override
        public int getItemCount() {
            return mAList.size();
        }

        public MainPage.MyAdapter.ViewHolder getItemView(int index) {
            return itemViewList.get(index);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout rootView;
            TextView IDLabel;
            TextView titleLabel;
            TextView patientNameLabel;
            TextView resultLabel;
            TextView doctorNameLabel;
            TextView checkDateLabel;
            TextView reportDateLabel;

            public ViewHolder(View itemView) {
                super(itemView);
                rootView = itemView.findViewById(R.id.root_view);
                IDLabel = itemView.findViewById(R.id.IDLabel);
                titleLabel = itemView.findViewById(R.id.titleLabel);
                patientNameLabel = itemView.findViewById(R.id.patientNameLabel);
                resultLabel = itemView.findViewById(R.id.resultLabel);
                doctorNameLabel = itemView.findViewById(R.id.doctorNameLabel);
                checkDateLabel = itemView.findViewById(R.id.checkDateLabel);
                reportDateLabel = itemView.findViewById(R.id.reportDateLabel);
            }
        }
    }
}
