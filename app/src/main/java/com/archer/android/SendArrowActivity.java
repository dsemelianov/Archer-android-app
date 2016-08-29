package com.archer.android;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SendArrowActivity extends ActionBarActivity {

    private static final int ITEM_VIEW_TYPE_SEPARATOR = 0;
    private static final int ITEM_VIEW_TYPE_PERSON = 1;
    private static final int ITEM_VIEW_TYPE_COUNT = 2;

    private ContactsAdapter listAdapter;

    SwipeRefreshLayout swipeLayout;

    ListView mListView;

    ArrayList<Arrow> mList = new ArrayList<>();

    String mMID;
    String mPhoneNumber;

    EditText mSearchBar;
    RelativeLayout mEmptyQuery;
    ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_arrow);
        setTheme(R.style.Theme_PointMe);
        getSupportActionBar().hide();

        /*ContentResolver cr = getContentResolver();
        final String[] projection = {Contacts.People.NAME, Contacts.People.NUMBER };
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

                if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                Toast.makeText(SendArrowActivity.this, id, Toast.LENGTH_SHORT).show();
                String name = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    //Query phone here.  Covered next
                }
            }
        }
*/
        SharedPreferences shared = getSharedPreferences("shared", MODE_PRIVATE);
        mMID = shared.getString("mid", "null");
        mPhoneNumber = shared.getString("phone", "null");


        mEmptyQuery = (RelativeLayout) findViewById(R.id.empty_query);

        mListView = (ListView) findViewById(R.id.list);
        listAdapter = new ContactsAdapter();
        mListView.setAdapter(listAdapter);

        mProgress = (ProgressBar) findViewById(R.id.progress);

        mSearchBar = (EditText) findViewById(R.id.phone_number_field);
        mSearchBar.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {

               // Toast.makeText(getBaseContext(), s.toString(), Toast.LENGTH_LONG).show();

                String [] requestedColumns = {
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                        //ContactsContract.Contacts.DISPLAY_NAME,
                        //ContactsContract.PhoneLookup.NUMBER
                        //Contacts.Phones.NAME,
                        //Contacts.Phones.NUMBER
                };

                Cursor contacts = getContentResolver().query(
                        //Contacts.Phones.CONTENT_URI,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        requestedColumns,
                        //   Contacts.Phones.NAME + "='" + "Reed" + "'",
                        // Contacts.Phones.NAME + "LIKE '%'" + "Reed" + "'%'",
                        // Contacts.Phones.NAME + "LIKE'" + "Reed" + "'",
                        //    "'" + Contacts.Phones.NAME + "' LIKE '%Reed%'",
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE '%" + s.toString().trim() + "%'",
                        null, null);

                mList.clear();

                if (contacts.getCount() > 0) {
                    mEmptyQuery.setVisibility(View.GONE);
                    while (contacts.moveToNext()) {

                        Arrow mContact = new Arrow("PERSON");

                        String mName = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        //String mName = contacts.getString(contacts.getColumnIndex(Contacts.Phones.NAME));
                        //Toast.makeText(SendArrowActivity.this, mName, Toast.LENGTH_SHORT).show();
                        mContact.setName(mName);

                        String mNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        //Toast.makeText(SendArrowActivity.this, mNumber, Toast.LENGTH_SHORT).show();
                        mContact.setNumber(mNumber);

                        mList.add(mContact);

                    }

                } else {
                    mEmptyQuery.setVisibility(View.VISIBLE);
                }

                listAdapter.notifyDataSetChanged();

            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        swipeLayout.setColorSchemeColors(
                getResources().getColor(R.color.arrow_blue),
                getResources().getColor(R.color.request_orange),
                getResources().getColor(R.color.sponsored_purple)
        );

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                     //   new GetArrowsTask().execute("http://pointme-hogueyy.c9users.io/api/arrows/" + mMID + ".json?k=hdj36dHyFB47dnG7h5FrEEW6E8d99j00JpD3a1M");
                    }
                }, 5000);
            }
        });

        //new GetArrowsTask().execute("http://pointme-hogueyy.c9users.io/api/arrows/" + mMID + ".json?k=hdj36dHyFB47dnG7h5FrEEW6E8d99j00JpD3a1M");


    }

    private class ContactsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            //return OBJECTS.length;
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            //return OBJECTS[position];
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return ITEM_VIEW_TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            //if (OBJECTS[position] instanceof ArrowListItem) {
            if (mList.get(position).getType().equals("PERSON")) {
                return ITEM_VIEW_TYPE_PERSON;
           // } else if (mList.get(position).getType().equals("PLACE")) {
              //  return ITEM_VIEW_TYPE_PLACE;
            } else {
                return ITEM_VIEW_TYPE_SEPARATOR;
            }
        }

        @Override
        public boolean isEnabled(int position) {
            // A separator cannot be clicked !
            if (getItemViewType(position) == ITEM_VIEW_TYPE_SEPARATOR) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final int type = getItemViewType(position);

            // First, let's create a new convertView if needed. You can also
            // create a ViewHolder to speed up changes if you want ;)
            if (convertView == null) {
                if (type == ITEM_VIEW_TYPE_PERSON) {
                    convertView = LayoutInflater.from(SendArrowActivity.this).inflate(R.layout.list_item_search, parent, false);
                //} else if (type == ITEM_VIEW_TYPE_PLACE) {
                //    convertView = LayoutInflater.from(SendArrowActivity.this).inflate(R.layout.search_place_list_item, parent, false);
                } else {
                    convertView = LayoutInflater.from(SendArrowActivity.this).inflate(R.layout.list_item_separator, parent, false);
                }
            }

            // We can now fill the list item view with the appropriate data.
            final Arrow arrow = (Arrow) getItem(position);
            if (type == ITEM_VIEW_TYPE_PERSON) {

                ((TextView) convertView.findViewById(R.id.name)).setText(arrow.getName());

                RelativeLayout mBody = (RelativeLayout) convertView.findViewById(R.id.body);
                mBody.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String mOtherNumber = arrow.getNumber();
                        mOtherNumber = mOtherNumber.trim();
                        mOtherNumber = mOtherNumber.replaceAll( "[^\\d]", "" );
                        if (mOtherNumber.length() == 11) {
                            StringBuilder mBuilder = new StringBuilder(mOtherNumber);
                            mBuilder.deleteCharAt(0);
                            mOtherNumber = mBuilder.toString();
                        }
                        SendArrowDialog mDialogue = new SendArrowDialog(SendArrowActivity.this, mPhoneNumber, arrow.getName(), mOtherNumber, mMID);
                        mDialogue.show();

                    }
                });

            } else {
                ((TextView) convertView.findViewById(R.id.title)).setText(arrow.getName());
            }

            return convertView;
        }

    }
    // convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

}