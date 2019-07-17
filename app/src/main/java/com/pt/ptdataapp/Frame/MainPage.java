package com.pt.ptdataapp.Frame;

import android.content.Context;
import android.graphics.Typeface;
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

import com.pt.ptdataapp.MainActivity;
import com.pt.ptdataapp.Model.DataManager;
import com.pt.ptdataapp.Model.FileEntity;
import com.pt.ptdataapp.Model.PatientInfo;
import com.pt.ptdataapp.R;
import com.pt.ptdataapp.uiUtils.OnViewPagerListener;
import com.pt.ptdataapp.uiUtils.ViewPagerLayoutManager;
import com.pt.ptdataapp.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainPage extends Fragment {

    private View rootView;
    private static final String TAG = "MainPage";
    private RecyclerView mRecyclerView;
    private MainPage.MyAdapter mAdapter;
    private ViewPagerLayoutManager mLayoutManager;
    private int curSelectIndex = 0;
    private MainActivity activityContext;
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

    public void SetContext(MainActivity context)
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

    public void onBackPressed() {

        if (activityContext != null)
        {
            if (DataManager.getInstance().CurReadChildPathBeforeMainPage != null)
            {
                File childFile = new File(DataManager.getInstance().CurReadChildPathBeforeMainPage);
                activityContext.OnShowFileExplorePage(childFile.getParent(), childFile);
            }
        }
    }

    public void ScrollToTop()
    {
        if (mAdapter != null && mAdapter.mAList.size() > 0)
        {
            mRecyclerView.smoothScrollToPosition(0);
        }
    }
    private int cacheScrollToIndex = -1;
    public void SafeScrollToIndex(int index)
    {
        if (mAdapter == null)
        {
            cacheScrollToIndex = index;
        }
        else
        {
            ScrollToIndex(index);
        }
    }
    public void ScrollToIndex(int index)
    {
        if (mAdapter != null && mAdapter.mAList.size() > index)
        {
            mRecyclerView.scrollToPosition(index);
            curSelectIndex = index;
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
//                            mAdapter.mAList = DataManager.getInstance().GetPatientList();
                            Log.d(TAG, "mAapter item count =" + mAdapter.getItemCount());
                            mAdapter.notifyDataSetChanged();
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        if (cacheScrollToIndex >= 0)
        {
            ScrollToIndex(cacheScrollToIndex);
            cacheScrollToIndex = -1;
        }
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
        MainPage.MyAdapter.ViewHolder viewHolder = mAdapter.getItemView(curSelectIndex);
        if (viewHolder != null)
        {
            List<String> printList = new ArrayList<>();
            printList.add(viewHolder.titleLabel.getText().toString());
            printList.add(viewHolder.IDLabel.getText().toString());
            printList.add(viewHolder.patientNameLabel.getText().toString());
            printList.add(viewHolder.testIDLabel.getText().toString());
            printList.add(viewHolder.ptLabel.getText().toString());
            printList.add(viewHolder.resultLabel.getText().toString());
            printList.add(viewHolder.doctorNameLabel.getText().toString());
            printList.add(viewHolder.checkDateLabel.getText().toString());
            printList.add(viewHolder.testTimeLabel.getText().toString());
            printList.add(viewHolder.reportDateLabel.getText().toString());

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
                Log.d(TAG, pInfo.checkDate + " " + pInfo.checkResult + " " + pInfo.testID);
                holder.titleLabel.setText(pInfo.title);

                int inputType = InputType.TYPE_CLASS_TEXT;
                holder.IDLabel.setText(pInfo.ID);
                holder.IDLabel.setInputType(inputType);

                inputType = (pInfo.patientName.length() > 0) ? InputType.TYPE_NULL : InputType.TYPE_CLASS_TEXT;
                holder.patientNameLabel.setInputType(inputType);
                holder.patientNameLabel.setText(pInfo.patientName);

                boolean isError = (pInfo.errorCode.replace(" ", "").length() > 0);
                if (isError)
                {
                    holder.resultLabel.setText(pInfo.errorCode);
                    holder.ptLabel.setText(pInfo.errorCode);
                }
                else
                {
                    holder.resultLabel.setText(pInfo.checkResult);
                    holder.ptLabel.setText(pInfo.Pt);
                }

                inputType = (pInfo.doctorName.length() > 0) ? InputType.TYPE_NULL : InputType.TYPE_CLASS_TEXT;
                holder.doctorNameLabel.setInputType(inputType);
                holder.doctorNameLabel.setText(pInfo.doctorName);

                holder.checkDateLabel.setText(pInfo.checkDate);

                holder.reportDateLabel.setText(pInfo.reportDate);

                holder.testIDLabel.setText(pInfo.testID);

                holder.testTimeLabel.setText(pInfo.testTime);
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
            TextView IDLabel;
            TextView titleLabel;
            EditText patientNameLabel;
            TextView resultLabel;
            EditText doctorNameLabel;
            TextView checkDateLabel;
            TextView reportDateLabel;
            TextView testIDLabel;
            TextView testTimeLabel;
            TextView ptLabel;

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
                testTimeLabel = itemView.findViewById(R.id.testTimeLabel);
                ptLabel = itemView.findViewById(R.id.ptLabel);
                testIDLabel = itemView.findViewById(R.id.testIDLabel);

                Typeface typeface = Typeface.createFromAsset(Utils.getContext().getAssets(), "font/arcena.ttf");
                titleLabel.setTypeface(typeface);
            }
        }
    }
}
