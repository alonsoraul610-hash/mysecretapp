package com.mi.appanuncios;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public abstract class BaseActivity extends AppCompatActivity {

    protected void actualizarHeaderUsuario(NavigationView navigationView) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView textUserName = headerView.findViewById(R.id.textUserName);
            ImageView imageProfile = headerView.findViewById(R.id.imageProfile);

            String nombreUsuario = user.getDisplayName();
            if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
                textUserName.setText(nombreUsuario);
            }

            Uri fotoPerfil = user.getPhotoUrl();
            if (fotoPerfil != null) {
                Glide.with(this).load(fotoPerfil).placeholder(R.drawable.ic_persona).into(imageProfile);
            } else {
                imageProfile.setImageResource(R.drawable.ic_persona);
            }
        }
    }
}

