package net.efedos.example.infinitescroll;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
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

public class ListViewLikeChatActivity extends AppCompatActivity {

    private static final String TAG = "ListViewLikeChat";
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
    private static int currentPage = 0;
    private static int visibleThreshold = 20;
    //private static boolean isFirstCheck = true;
    private View footer;
    private static ProgressBar pBar;
    //private static EndlessScrollListener endlessScrollListener;
    private TmpHolder tmpHolder;
    private static SwipeRefreshLayout swipeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list = new ArrayList<>();
        _this = this;
        //endlessScrollListener = new EndlessScrollListener(15);
        setContentView(R.layout.activity_list_view_like_chat);
        setUpProgressDialog();
        setUpListView();

        initPullToRefresh();
    }

    private void initPullToRefresh() {
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
    }

    private void setUpProgressDialog() {
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Cargando");
        pDialog.setIndeterminate(true);

    }

    private void setUpListView() {
        Log.i(TAG, "setUpListView()");
        listView = (ListView) findViewById(R.id.listView);
        LayoutInflater inflater = getLayoutInflater();
        footer = inflater.inflate(R.layout.inc_footer, null, false);
        //pBar = (ProgressBar) footer.findViewById(R.id.progressBar);
        //listView.addHeaderView(footer);

        //pBar.setVisibility(View.GONE);
        loadData();
    }

    private static void loadData() {
        Log.i(TAG,"loadData()");
        if (currentPage == 0) pDialog.show();
        currentPage++;
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //list.clear();
                Collections.reverse(list);
                for (i = i; i <= (visibleThreshold * currentPage); ++i) {
                    Log.i(TAG,"row: " + i);
                    list.add("row: " + i);
                }
                //pBar.setVisibility(View.GONE);
                loadListView();
            }
        }, 1000);
    }

    private static void loadListView() {
        Log.i(TAG, "loadListView()");

        final int firstPosition = listView.getFirstVisiblePosition();
        if ( adapter == null ) {
            Collections.reverse(list);
            Log.i(TAG, "adapter == null");
            adapter = new ItemAdapter(_this,list);
            listView.setAdapter(adapter);
        }else {
            Log.i(TAG, "adapter.refill()");
            Collections.reverse(list);
            adapter.reset();

            listView.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG,"firstPosition: "+firstPosition);
                    //listView.smoothScrollToPosition(visibleThreshold + 1, listView.getTop());
                    listView.setSelection(firstPosition+(visibleThreshold));
                }
            });

        }

        hasCallback = false;
        swipeLayout.setRefreshing(false);

        if ( pDialog.isShowing() ) pDialog.cancel();
    }

    // region ScrollListener
    /*public class EndlessScrollListener implements AbsListView.OnScrollListener {

        private static final String TAG = "EndlessScrollListener";
        private int visibleThreshold = 15;
        private int currentPage = 0;
        private int previousTotal = 0;


        public EndlessScrollListener() {
        }
        public EndlessScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
            previousTotal = visibleThreshold;
            //isFirstCheck = true;
        }

        @Override
        public void onScroll(AbsListView view, int hiddenItemsCount,
                             int visibleItemCount, int totalItemCount) {
            tmpHolder = new TmpHolder(hiddenItemsCount, visibleItemCount, totalItemCount, hasCallback);
            Log.i(TAG,"tmpHolder: "+tmpHolder);

            if (hiddenItemsCount == 0 && visibleItemCount > 0 && !adapter.endReached() && !hasCallback) { //check if we've reached the bottom
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
                            scrollToTop();
                            //loadData();
                        }
                    }
                }, 300);
                hasCallback = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            Log.i(TAG, "onScrollStateChanged()");
            Log.i(TAG,"tmpHolder: "+tmpHolder);
        }
    }*/
    // endregion

    class TmpHolder {
        int hiddenItemsCount;
        int visibleItemCount;
        int totalItemCount;
        boolean hasCallback;

        public TmpHolder(int hiddenItemsCount, int visibleItemCount, int totalItemCount, boolean hasCallback) {
            this.hiddenItemsCount = hiddenItemsCount;
            this.visibleItemCount = visibleItemCount;
            this.totalItemCount = totalItemCount;
            this.hasCallback = hasCallback;
        }

        @Override
        public String toString() {
            return "TmpHolder{" +
                    "hiddenItemsCount=" + hiddenItemsCount +
                    ", visibleItemCount=" + visibleItemCount +
                    ", totalItemCount=" + totalItemCount +
                    ", hasCallback=" + hasCallback +
                    '}';
        }
    }

    private static void scrollToTop() {
        Log.i(TAG,"scrollToTop()");
        listView.setSelection(adapter.getCount() - 1);
    }

    public static class ItemAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<String> items;
        private Context context;
        private int count;
        private List<String> itemsReversed;

        public ItemAdapter(Context context, List<String> items) {
            super();
            inflater = LayoutInflater.from(context);
            this.context = context;
            this.items = items;
            itemsReversed = this.items;
            //Collections.reverse(itemsReversed);
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

            holder.itemName.setText("Name: " + itemsReversed.get(position));
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
            Log.i(TAG,"showMore()");
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
            Log.i(TAG,"endReached()");
            return count == this.items.size();
        }

        /**
         * Sets the ListView back to its initial count number
         */
        public void reset(){
            //count = startCount;
            itemsReversed = this.items;
            //Collections.reverse(itemsReversed);
            notifyDataSetChanged();
        }

        private static class ItemViewHolder {
            TextView itemName;
        }
    }
}
