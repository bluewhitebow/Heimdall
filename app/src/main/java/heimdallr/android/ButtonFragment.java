package heimdallr.android;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ButtonFragment  extends Fragment {

    private Button click;
    private View view;
    private ButtonFragment.OnFragmentInteractionListener mListener;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_button, container, false);
        click = (Button) view.findViewById(R.id.click);
        click.setVisibility(View.VISIBLE);

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainMenuActivity)getActivity()).showHomeFragment();


            }
        });

        //设置字体
        TextView heimdallr = view.findViewById(R.id.fragment_heimdallr);
        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(),"fonts/标题字体.ttf");
        heimdallr.setTypeface(typeFace);
        click.setTypeface(typeFace);

        return view;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ButtonFragment.OnFragmentInteractionListener) {
            mListener = (ButtonFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    public interface OnFragmentInteractionListener {
    }
}
