package net.efedos.example.infinitescroll;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    //private int page = 0;
    //private int rowsPerPage = 15;
    private static ArrayList<String> list;
    private static Handler handler;
    private static ListView listView;
    private static ProgressDialog pDialog;
    private static ItemAdapter adapter;
    private static boolean loading = true;
    private static int lastViewedPosition;
    private static int topOffset;
    private static Context _this;
    private static int i = 0;
    private static boolean hasCallback;
    //private static boolean isFirstCheck = true;
    private View footer;
    private static ProgressBar pBar;
    private static EndlessScrollListener endlessScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list = new ArrayList<>();
        _this = this;
        endlessScrollListener = new EndlessScrollListener(15);
        setContentView(R.layout.activity_main);
        setUpProgressDialog();
        setUpListView();
    }

    private void setUpProgressDialog() {
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Cargando");
        pDialog.setIndeterminate(true);

    }

    private void setUpListView() {
        Log.i(TAG,"setUpListView()");
        listView = (ListView) findViewById(R.id.listView);
        LayoutInflater inflater = getLayoutInflater();
        footer = inflater.inflate(R.layout.inc_footer, null, false);
        pBar = (ProgressBar) footer.findViewById(R.id.progressBar);
        listView.addFooterView(footer);

        pBar.setVisibility(View.GONE);
        loadData();
    }

    private static void loadData() {
        Log.i(TAG,"loadData()");
        if (EndlessScrollListener.currentPage == 0) pDialog.show();
        EndlessScrollListener.currentPage++;
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //list.clear();
                for (i = i; i < (EndlessScrollListener.visibleThreshold * EndlessScrollListener.currentPage)-1; ++i) {
                    Log.i(TAG,"row: " + i);
                    list.add("row: " + i);
                }
                pBar.setVisibility(View.GONE);
                loadListView();
            }
        }, 1000);
    }

    private static void loadListView() {
        Log.i(TAG, "loadListView()");

        if ( adapter == null ) {
            //Collections.reverse(list);
            Log.i(TAG, "adapter == null");
            adapter = new ItemAdapter(_this,list);
            listView.setAdapter(adapter);
            listView.setOnScrollListener(endlessScrollListener);
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loading = false;
                }
            }, 1000);
        }else {
            Log.i(TAG, "adapter.refill()");
            //adapter.addItems(list);
            adapter.notifyDataSetChanged();
            //loading = false;
        }
        hasCallback = false;

        if ( pDialog.isShowing() ) pDialog.cancel();
    }

    public static class EndlessScrollListener implements AbsListView.OnScrollListener {

        private static final String TAG = "EndlessScrollListener";
        private static int visibleThreshold = 15;
        private static int currentPage = 0;
        private int previousTotal = 0;


        public EndlessScrollListener() {
        }
        public EndlessScrollListener(int visibleThreshold) {
            EndlessScrollListener.visibleThreshold = visibleThreshold;
            previousTotal = visibleThreshold;
            //isFirstCheck = true;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

            if (firstVisibleItem + visibleItemCount == totalItemCount && !adapter.endReached() && !hasCallback) { //check if we've reached the bottom
                Log.i(TAG, "onScroll() condition success");
                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        Log.i(TAG, "Run()");
                        boolean noMoreToShow = adapter.showMore(); //show more views and find out if
                        if (noMoreToShow) {
                            pBar.setVisibility(View.GONE);
                            hasCallback = false;
                        } else {
                            pBar.setVisibility(View.VISIBLE);
                            scrollToBottom();
                            loadData();
                        }
                    }
                }, 300);
                hasCallback = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            Log.i(TAG, "onScrollStateChanged()");
        }
    }

    private static void scrollToBottom() {
        listView.setSelection(adapter.getCount() - 1);
    }

    public static class ItemAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<String> items;
        private Context context;
        private int count;

        public ItemAdapter(Context context, List<String> items) {
            super();
            inflater = LayoutInflater.from(context);
            this.context = context;
            this.items = items;

        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemViewHolder holder = null;
            if(convertView == null) {
                holder = new ItemViewHolder();
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent,false);
                holder.itemName = (TextView) convertView.findViewById(android.R.id.text1);
                convertView.setTag(holder);
            } else {
                holder = (ItemViewHolder) convertView.getTag();
            }
            holder.itemName.setText("Name: " + items.get(position));
            if(position % 2 == 0) {
                convertView.setBackgroundColor(context.getResources().getColor(R.color.evenRowColor));
            } else {
                convertView.setBackgroundColor(context.getResources().getColor(R.color.oddRowColor));
            }
            return convertView;
        }

        public void refill(List<String> items) {
            this.items.clear();
            this.items.addAll(items);
            notifyDataSetChanged();
        }

        public void addItems(List<String> items) {
            this.items.addAll(items);
            //this.items.addAll(items);
            notifyDataSetChanged();
        }

        /**
         * Show more views, or the bottom
         * @return true if the entire data set is being displayed, false otherwise
         */
        public boolean showMore(){
            if(count == this.items.size()) {
                return true;
            }else{
                count = Math.min(count, this.items.size()); //don't go past the end
                notifyDataSetChanged(); //the count size has changed, so notify the super of the change
                return endReached();
            }
        }

        /**
         * @return true if then entire data set is being displayed, false otherwise
         */
        public boolean endReached(){
            return count == this.items.size();
        }

        /**
         * Sets the ListView back to its initial count number
         */
        public void reset(){
            //count = startCount;
            notifyDataSetChanged();
        }

        private static class ItemViewHolder {
            TextView itemName;
        }
    }
}
