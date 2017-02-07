package com.forthe.xlog;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.forthe.xlog.frame.ColorPool;
import com.forthe.xlog.frame.PanelContainer;
import com.forthe.xlog.panel.FilterPanel;
import com.forthe.xlog.tools.TouchListView;

class XLogWindow implements View.OnClickListener {
    private Activity activity;
    private PopupWindow popupWindow;
    private TextView tv_config_list, tv_log_list, tv_error_list, tv_title_left,
            tv_title_right, tv_title_center;
    private TouchListView lv;
    private XLogConfigAdapter configAdapter;
    private XLogAdapter logAdapter;
    private XLogCrashAdapter crashAdapter;
    private PanelContainer panelContainer;

    boolean isCurrentActivity(Activity activity){
        return activity != null && activity.equals(this.activity);
    }

    XLogWindow(Activity activity) {
        this.activity = activity;
        ColorPool.init(activity);
        View root = View.inflate(activity, R.layout.forthe_xlog_pop_window, null);
        initView(root);
        popupWindow = new PopupWindow(root, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT){
            @Override
            public void dismiss() {
                if(!panelContainer.isEmpty()) {
                    panelContainer.dismissChild();
                }else{
                    super.dismiss();
                }
            }
        };
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xffffffff));
    }

    void show(){
        show(XLog.PAGE_CONFIG);
    }

    void show(int page){
        if(popupWindow.isShowing()){
            return;
        }

        View decorView = activity.getWindow().getDecorView();
        if(decorView.getWindowToken() == null){
            return;
        }

        popupWindow.showAtLocation(decorView, Gravity.TOP, 0, 0);
        switchPage(page);
    }

    private void initView(View root){
        RelativeLayout rl_panel_container = (RelativeLayout) root.findViewById(R.id.rl_panel_container);
        panelContainer = new PanelContainer(rl_panel_container);

        configAdapter = new XLogConfigAdapter(activity);
        logAdapter = new XLogAdapter(activity, panelContainer);
        crashAdapter = new XLogCrashAdapter(activity, panelContainer);

        lv = (TouchListView) root.findViewById(R.id.lv);
        tv_config_list = (TextView) root.findViewById(R.id.tv_config_list);
        tv_log_list = (TextView) root.findViewById(R.id.tv_log_list);
        tv_error_list = (TextView) root.findViewById(R.id.tv_error_list);


        tv_title_left = (TextView) root.findViewById(R.id.tv_title_left);
        tv_title_right = (TextView) root.findViewById(R.id.tv_title_right);
        tv_title_center = (TextView) root.findViewById(R.id.tv_title_center);

        root.findViewById(R.id.tv_back).setOnClickListener(this);
        tv_config_list.setOnClickListener(this);
        tv_log_list.setOnClickListener(this);
        tv_error_list.setOnClickListener(this);
        tv_title_right.setOnClickListener(this);
        tv_title_left.setOnClickListener(this);

        lv.setDownTouchListener(new TouchListView.OnDownTouchListener() {
            @Override
            public void onDownTouch() {
                if(panelContainer != null){
                    panelContainer.dismissAllChild();
                }
            }
        });
    }


    void dismiss(){
        if(popupWindow.isShowing()){
            popupWindow.dismiss();
        }
    }


    private void onBackPressed(){
        if(!panelContainer.isEmpty()) {
            panelContainer.dismissChild();
        }else{
            dismiss();
        }
    }

    private FilterPanel filterPanel;
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(R.id.tv_back == id) {
            onBackPressed();
        } else if(R.id.tv_title_right == id){
            if(currentPage == XLog.PAGE_LOGS){
                XLog.clearLog();
                logAdapter.clear();
            }
        } else if(R.id.tv_title_left == id){
            if(currentPage == XLog.PAGE_LOGS){
                if(null == filterPanel){
                    filterPanel = new FilterPanel(logAdapter);
                }

                if(!filterPanel.isShow()){
                    panelContainer.showPanel(filterPanel);
                }else{
                    panelContainer.dismissPanel(filterPanel);
                }
            }
        } else if(R.id.tv_config_list == id){
            switchPage(XLog.PAGE_CONFIG);
        } else if(R.id.tv_log_list == id){
            switchPage(XLog.PAGE_LOGS);
        } else if(R.id.tv_error_list == id){
            switchPage(XLog.PAGE_CRASH);
        }
    }

    /**
     * @param page
     * 0  switches
     * 1  Logs
     * 2  Error Logs
     */
    private int currentPage;
    private void switchPage(int page){
        panelContainer.dismissAllChild();
        currentPage = page;
        tv_config_list.setSelected(false);
        tv_log_list.setSelected(false);
        tv_error_list.setSelected(false);
        switch (page){
            case XLog.PAGE_CONFIG:
                tv_config_list.setSelected(true);
                tv_title_center.setText("DEBug");
                tv_title_right.setText("");
                tv_title_left.setText("");
                lv.setAdapter(configAdapter);
                break;
            case XLog.PAGE_LOGS:
                tv_log_list.setSelected(true);
                tv_title_left.setText("filter");
                tv_title_center.setText("Log");
                tv_title_right.setText("clear");
                lv.setAdapter(logAdapter);
                lv.setSelection(logAdapter.getCount());
                break;
            case XLog.PAGE_CRASH:
                tv_error_list.setSelected(true);
                tv_title_center.setText("Crash");
                tv_title_right.setText("");
                tv_title_left.setText("");
                lv.setAdapter(crashAdapter);
                crashAdapter.loadData();
                break;
            default:
        }
    }
}