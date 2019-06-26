package com.pt.ptdataapp.Frame;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pt.ptdataapp.MainActivity;
import com.pt.ptdataapp.Model.DataManager;
import com.pt.ptdataapp.Model.FileEntity;
import com.pt.ptdataapp.Model.LocalFileModel;
import com.pt.ptdataapp.R;
import com.pt.ptdataapp.fileUtil.FileUtil;
import com.pt.ptdataapp.uiUtils.OnViewPagerListener;
import com.pt.ptdataapp.uiUtils.RecyclerViewWithContextMenu;
import com.pt.ptdataapp.uiUtils.ViewPagerLayoutManager;
import com.pt.ptdataapp.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StationListPage extends Fragment {

    private View rootView;
    private static final String TAG = "StationListPage";
    private RecyclerViewWithContextMenu mRecyclerView;
    private StationListPage.MyAdapter mAdapter;
    private ViewPagerLayoutManager mLayoutManager;
    private int curSelectIndex = 0;
    private MainActivity activityContext;
    private Handler mHandler;
    private ArrayList<FileEntity> mList;
    private String rootFilePath;
    private int menuSelectPosition = -1;

    public StationListPage()
    {
    }
    //1、定义接口
    public interface OnFileClick {
        public void onClick(String clickFilePath, int position);
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

    public void SetContext(MainActivity context)
    {
        activityContext = context;

    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu,v,menuInfo);
        //设置Menu显示内容
        menu.setHeaderTitle("文件操作");
        menu.add(1,1,1,"删除");
        File curCopyTargetUsbFile = DataManager.getInstance().GetCopyUsbPath();
        if (curCopyTargetUsbFile != null)
        {
            menu.add(1,2,1,"复制至" + curCopyTargetUsbFile.getName());
        }
    }

    public boolean onContextItemSelected(MenuItem item){
//        RecyclerViewWithContextMenu.RecyclerViewContextInfo info = (RecyclerViewWithContextMenu.RecyclerViewContextInfo) item.getMenuInfo();
//        if (info != null && info.getPosition() != -1 ) {
            menuSelectPosition = curSelectIndex;
            switch (item.getItemId()) {
                case 1:
                    if (menuSelectPosition >= 0) {
                        final FileEntity entity = mList.get(menuSelectPosition);
                        if (entity != null) {
                            if (entity.getFileType() == FileEntity.Type.FILE) {
                                FileUtil.deletefile(entity.getFilePath());
                            } else {
                                FileUtil.deleteDirectory(entity.getFilePath());
                            }

                            mList.remove(menuSelectPosition);
                            menuSelectPosition = -1;
                            mHandler.sendEmptyMessage(1);
                        }
                    }

                    break;
                case 2:
                    if (menuSelectPosition >= 0) {
                        File curCopyTargetUsbFile = DataManager.getInstance().GetCopyUsbPath();
                        if (curCopyTargetUsbFile == null || !curCopyTargetUsbFile.exists()) {
                            Toast.makeText(Utils.getContext(), "复制目标目录不存在", Toast.LENGTH_SHORT).show();
                            return super.onContextItemSelected(item);
                        }

                        final FileEntity entity = mList.get(menuSelectPosition);
                        if (entity != null) {
                            String sourcePath = entity.getFilePath();
                            String targetPath = curCopyTargetUsbFile.getAbsolutePath() + File.separator + entity.getFileName();
                            activityContext.ShowDialog("正在复制文件,请勿拔出USB设备");
                            if (entity.getFileType() == FileEntity.Type.FLODER) {

                                FileUtil.copyFolder(sourcePath, targetPath, null);
                            } else {
                                FileUtil.copyFile(sourcePath, targetPath);
                            }
                            activityContext.HideDialog();
                            menuSelectPosition = -1;
                        }
                    }
                    break;
            }
//        }
        return super.onContextItemSelected(item);
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
                            mAdapter.RefreshList(mList);
                            mAdapter.notifyDataSetChanged();
                        }
                        break;
                    case 2:
                        if(onFileClick !=null){
                            onFileClick.onClick((String)msg.obj, msg.arg1);
                        }
                    default:
                        break;
                }
            }
        };
        registerForContextMenu(mRecyclerView);

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
        public void RefreshList(List<FileEntity> list)
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
            FileEntity fileEntity = mAList.get(position);
            if (fileEntity != null)
            {
                holder.stationImg.setVisibility(View.VISIBLE);
                holder.stationName.setVisibility(View.VISIBLE);
                holder.stationName.setText(fileEntity.getFileName());
                return;
            }

            holder.stationImg.setVisibility(View.INVISIBLE);
            holder.stationName.setVisibility(View.INVISIBLE);
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

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView stationName;
            ImageView stationImg;
            public ViewHolder(View itemView) {
                super(itemView);
                stationName = itemView.findViewById(R.id.stationNameLabel);
                stationImg = itemView.findViewById(R.id.stationImg);
                itemView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        int pos = getAdapterPosition();
                        if (pos < 0)
                        {
                            pos = 0;
                        }
                        final FileEntity entity = mAList.get(pos);
                        if (entity != null) {
                                Message msg = new Message();
                                msg.what = 2;
                                msg.obj = entity.getFilePath();
                                msg.arg1 = pos;
                                mHandler.sendMessage(msg);
                        }
                    }
                });
            }
        }
    }
}

