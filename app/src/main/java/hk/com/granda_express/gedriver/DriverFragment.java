package hk.com.granda_express.gedriver;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static android.R.attr.button;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DriverFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DriverFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverFragment extends DialogFragment {
    private OnFragmentInteractionListener mListener;
    Button button;

    public DriverFragment() {
        // Required empty public constructor
    }

    public static DriverFragment newInstance() {
        DriverFragment fragment = new DriverFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(R.string.action_driver);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_driver, container, false);
        button = (Button) view.findViewById(R.id.button_driver);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner driverSpinner = (Spinner) getView().findViewById(R.id.driverSpinner);
                ArrayList<Driver> drivers = mListener.getDrivers();
                for (int i = 0; i < drivers.size(); i++) {
                    Driver driver = drivers.get(i);
                    if (driver.Title == driverSpinner.getSelectedItem().toString()) {
                        mListener.setDriverId(driver.id);
                    }
                }
                getDialog().dismiss();
            }
        });

        Spinner spinner = (Spinner) view.findViewById(R.id.driverSpinner);
        ArrayList<Driver> drivers = mListener.getDrivers();
        int driverId = mListener.getDriverId();
        String[] driverNames = new String[drivers.size()];
        int selected = -1;
        for (int i = 0; i < drivers.size(); i++) {
            driverNames[i] = drivers.get(i).Title;
            if (driverId == drivers.get(i).id) {
                selected = i;
            }
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(this.getContext(), android.R.layout.simple_list_item_1, driverNames);
        spinner.setAdapter(arrayAdapter);
        if (selected > -1) {
            spinner.setSelection(selected);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public int getDriverId();
        public ArrayList<Driver> getDrivers();
        public void setDriverId(int driverId);
    }
}
