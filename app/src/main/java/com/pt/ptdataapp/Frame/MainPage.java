package com.pt.ptdataapp.Frame;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
        if (rootView != null)
        {
            mHandler.sendEmptyMessage(1);
        }

    }

    public void ScrollToTop()
    {
        if (mAdapter != null && mAdapter.mAList.size() > 0)
        {
            mRecyclerView.smoothScrollToPosition(0);
        }
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
//                Log.e(TAG,"释放位置:"+position +" 下一页:"+isNext);

            }

            @Override
            public void onPageSelected(int position,boolean isEnd) {
//                Log.e(TAG,"选中位置:"+position+"  是否是滑动到底部:"+isEnd);
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
        MainPage.MyAdapter.ViewHolder viewHolder = mAdapter.getItemView(curSelectIndex);
        if (viewHolder != null)
        {
            List<String> printList = new ArrayList<>();
            printList.add(viewHolder.titleLabel.getText().toString());
            printList.add("ID:" + viewHolder.IDLabel.getText().toString());
            printList.add("姓名:" + viewHolder.patientNameLabel.getText().toString());
            printList.add("INR:" + viewHolder.resultLabel.getText().toString());
            printList.add("报告医生:" + viewHolder.doctorNameLabel.getText().toString());
            printList.add("检测日期:" + viewHolder.checkDateLabel.getText().toString());
            printList.add("报告日期:" + viewHolder.reportDateLabel.getText().toString());
            DataManager.getInstance().SavePrintContentList(printList);
        }

    }

    class MyAdapter extends RecyclerView.Adapter<MainPage.MyAdapter.ViewHolder> {
        private List<MainPage.MyAdapter.ViewHolder> itemViewList = new ArrayList<MainPage.MyAdapter.ViewHolder>();
        private List<PatientInfo> mAList;
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
                int inputType = (pInfo.ID.length() > 0) ? InputType.TYPE_NULL : InputType.TYPE_CLASS_NUMBER;
                holder.IDLabel.setInputType(inputType);
                holder.IDLabel.setText(pInfo.ID);

                inputType = (pInfo.patientName.length() > 0) ? InputType.TYPE_NULL : InputType.TYPE_CLASS_TEXT;
                holder.patientNameLabel.setInputType(inputType);
                holder.patientNameLabel.setText(pInfo.patientName);

                inputType = (pInfo.checkResult.length() > 0) ? InputType.TYPE_NULL : InputType.TYPE_CLASS_NUMBER;
                holder.resultLabel.setInputType(inputType);
                holder.resultLabel.setText(pInfo.checkResult);

                inputType = (pInfo.doctorName.length() > 0) ? InputType.TYPE_NULL : InputType.TYPE_CLASS_TEXT;
                holder.doctorNameLabel.setInputType(inputType);
                holder.doctorNameLabel.setText(pInfo.doctorName);

                inputType = (pInfo.checkDate.length() > 0) ? InputType.TYPE_NULL : InputType.TYPE_CLASS_TEXT;
                holder.checkDateLabel.setInputType(inputType);
                holder.checkDateLabel.setText(pInfo.checkDate);

                holder.reportDateLabel.setInputType(InputType.TYPE_NULL);
                holder.reportDateLabel.setText(pInfo.reportDate);
            }
        }

        @Override
        public int getItemCount() {
            return mAList.size();
        }

        public MainPage.MyAdapter.ViewHolder getItemView(int position) {
            for(ViewHolder view: itemViewList)
            {
                if (position == view.getLayoutPosition())
                {
                    return view;
                }
            }
            return null;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout rootView;
            EditText IDLabel;
            TextView titleLabel;
            EditText patientNameLabel;
            EditText resultLabel;
            EditText doctorNameLabel;
            EditText checkDateLabel;
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
