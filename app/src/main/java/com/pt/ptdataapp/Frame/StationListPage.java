package com.pt.ptdataapp.Frame;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.pt.ptdataapp.Model.FileEntity;
import com.pt.ptdataapp.Model.LocalFileModel;
import com.pt.ptdataapp.R;
import com.pt.ptdataapp.fileUtil.FileUtil;
import com.pt.ptdataapp.uiUtils.OnViewPagerListener;
import com.pt.ptdataapp.uiUtils.ViewPagerLayoutManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StationListPage extends Fragment {

    private View rootView;
    private static final String TAG = "StationListPage";
    private RecyclerView mRecyclerView;
    private StationListPage.MyAdapter mAdapter;
    private ViewPagerLayoutManager mLayoutManager;
    private int curSelectIndex = 0;
    private Context activityContext;
    private Handler mHandler;
    private ArrayList<FileEntity> mList;
    private String rootFilePath;

    public StationListPage()
    {
    }
    //1、定义接口
    public interface OnFileClick {
        public void onClick(String clickFilePath);
    }
    private OnFileClick onFileClick;//2、定义接口成员变量
    //定义接口变量的get方法
    public OnFileClick getOnFileClick() {
        return onFileClick;
    }
    //定义接口变量的set方法
    public void setOnFileClick(OnFileClick onFileClick) {
        this.onFileClick = onFileClick;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_station_list_page, container, false);
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
        }
    }

    private void initView() {
        mList = new ArrayList<>();
        if (mLayoutManager == null)
        {
            mLayoutManager = new ViewPagerLayoutManager(activityContext, OrientationHelper.HORIZONTAL);
            initListener();
        }
        if (mAdapter == null)
        {
            File currentFile = new File(Environment.getExternalStorageDirectory(), LocalFileModel.DATA_PATH);
            rootFilePath = currentFile.getAbsolutePath();
            mAdapter = new StationListPage.MyAdapter(mList);
        }
        if (mRecyclerView == null)
        {
            mRecyclerView = rootView.findViewById(R.id.stationListView);
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
                            File currentFile = new File(Environment.getExternalStorageDirectory(), LocalFileModel.DATA_PATH);
                            rootFilePath = currentFile.getAbsolutePath();
                            mAdapter = new StationListPage.MyAdapter(mList);
                            mRecyclerView.setAdapter(mAdapter);
                        }else{
                            mAdapter.notifyDataSetChanged();
                        }
                        break;
                    case 2:
                        if(onFileClick !=null){
                            onFileClick.onClick((String)msg.obj);
                        }
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
        getData(rootFilePath);
    }

    private void getData(final String path) {
        ((Activity)activityContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mList.clear();
                mList.addAll(FileUtil.FindAllFile(path, false));
                if (mList.size() == 0)
                {
                    mList.add(null);
                }
                mHandler.sendEmptyMessage(1);
            }
        });

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

    class MyAdapter extends RecyclerView.Adapter<StationListPage.MyAdapter.ViewHolder> {
        private List<StationListPage.MyAdapter.ViewHolder> itemViewList = new ArrayList<StationListPage.MyAdapter.ViewHolder>();
        private List<FileEntity> mAList;
        public MyAdapter(List<FileEntity> list)
        {
            mAList = list;
        }

        @Override
        public StationListPage.MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_station_info, parent, false);
            StationListPage.MyAdapter.ViewHolder viewHolder = new StationListPage.MyAdapter.ViewHolder(view);
            itemViewList.add(viewHolder);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(StationListPage.MyAdapter.ViewHolder holder, int position) {
            FileEntity file = mAList.get(position);
            if (file != null)
            {
                holder.stationName.setText(file.getFileName());
            }
            else
            {
                holder.stationName.setText("当前无设备数据");
            }
        }

        @Override
        public int getItemCount() {
            return mAList.size();
        }

        public StationListPage.MyAdapter.ViewHolder getItemView(int position) {
            for(StationListPage.MyAdapter.ViewHolder view: itemViewList)
            {
                if (position == view.getLayoutPosition())
                {
                    return view;
                }
            }
            return null;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            TextView stationName;

            public ViewHolder(View itemView) {
                super(itemView);
                stationName = itemView.findViewById(R.id.stationNameLabel);
            }

            @Override
            public void onClick(View v) {
                    int pos = getAdapterPosition();
                    final FileEntity entity = mList.get(pos);
                    if (entity != null)
                    {
                        Message msg = new Message();
                        msg.what = 2;
                        msg.obj = entity.getFilePath();
                        mHandler.sendMessage(msg);
                    }

            }
        }
    }
}

