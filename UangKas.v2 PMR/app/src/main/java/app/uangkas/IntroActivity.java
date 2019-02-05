package app.uangkas;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import app.uangkas.fragment.Intro1;
import app.uangkas.fragment.Intro2;
import app.uangkas.fragment.Intro3;

/**
 * Created by Irsyad on 8/25/2017.
 */

public class IntroActivity extends AppIntro {

    Intro1 intro1 = new Intro1();
    Intro2 intro2 = new Intro2();
    Intro3 intro3 = new Intro3();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();

        // Add your slide fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
//        addSlide(intro1);
//        addSlide(intro2);
//        addSlide(intro3);
//        addSlide(firstFragment);
//        addSlide(secondFragment);
//        addSlide(thirdFragment);
//        addSlide(fourthFragment);

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
//        addSlide(AppIntroFragment.newInstance(title, description, image, backgroundColor));
        addSlide(AppIntroFragment.newInstance("SELAMAT DATANG", "Saya siap menjadi Android Developer!", R.drawable.android, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("UANG KAS PMR", "TUGAS INTEGRASI SISTEM BERBASIS MOBILE DAN WEB DENGAN MENGGUNAKAN ANDROID STUDIO, PHP & MySQL",
                R.drawable.calc, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("", "", R.drawable.lazday, getResources().getColor(R.color.colorPrimary)));

        // OPTIONAL METHODS
        // Override bar/separator color.
//        setBarColor(Color.parseColor("#3F51B5"));
        setBarColor(getResources().getColor(R.color.colorPrimary));
//        setSeparatorColor(Color.parseColor("#2196F3"));
        setSeparatorColor(getResources().getColor(R.color.colorBackground));

        // Hide Skip/Done button.
        showSkipButton(true);
        setProgressButtonEnabled(true);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
        setVibrate(true);
        setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
