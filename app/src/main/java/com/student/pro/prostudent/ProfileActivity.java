package com.student.pro.prostudent;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

/*import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;*/


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    //Toolbar e Nav
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    // Imagem redonda
    private CircleImageView setupImage;
    //Codigo de atividade
    private static int RESULT_LOAD_IMAGE = 1;
    // Imagem
    private Uri filePath = null;
    //Firebase
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    //Logs
    private String TAG="DB_TEST",currentUser,picture_path;
    //TextView
    private TextView nameText,emailText,usernameText,surnameText,passText,confirmText;
    //EditText
    private EditText oldEdit,nameEdit,emailEdit,usernameEdit,surnameEdit,passEdit,confirmEdit;
    //ViewSwitcher
    private ViewSwitcher email_s,username_s,name_s,surname_s;
    //Buttons
    private Button emailB,userB,nameB,surnameB;

    public ProfileActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        /*
        ----Inicializações e Elementos----
         */
        //TextView
        this.nameText = findViewById(R.id.profile_name);
        this.emailText = findViewById(R.id.profile_email);
        this.usernameText = findViewById(R.id.profile_username);
        this.surnameText = findViewById(R.id.profile_surname);
        //EditText
        this.nameEdit = findViewById(R.id.profile_name2);
        this.emailEdit = findViewById(R.id.profile_email2);
        this.usernameEdit = findViewById(R.id.profile_username2);
        this.surnameEdit = findViewById(R.id.profile_surname2);
        this.oldEdit=findViewById(R.id.profile_old);
        this.passEdit=findViewById(R.id.profile_password);
        this.confirmEdit=findViewById(R.id.profile_confirm_pass);
        //ViewSwitcher
        this.email_s=findViewById(R.id.switch_email);
        this.username_s=findViewById(R.id.switch_username);
        this.name_s=findViewById(R.id.switch_name);
        this.surname_s=findViewById(R.id.switch_surname);
        //Buttons
        this.emailB=findViewById(R.id.but_email);
        this.userB=findViewById(R.id.but_user);
        this.nameB=findViewById(R.id.but_name);
        this.surnameB=findViewById(R.id.but_surname);

        //Imagem de Perfil default
        this.setupImage = findViewById(R.id.setup_image);

        //Base de dados
        this.mDatabase = FirebaseDatabase.getInstance().getReference("users");
        //Storage
        this.mStorageRef = FirebaseStorage.getInstance().getReference();
        //Utilizadores
        this.mAuth = FirebaseAuth.getInstance();
        user= mAuth.getCurrentUser();
        getUserData();
        //Toolbar & Navbar
        this.mToolbar = findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);
        this.mDrawerLayout = findViewById(R.id.drawerlayout);
        this.mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        this.mDrawerLayout.addDrawerListener(mToggle);
        this.mToggle.syncState();
        getSupportActionBar().setTitle(R.string.title_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
        ----- Click Listeners ----
         */
        //Eventos para trocar as views de TextView para EditText

        userB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             if(username_s.getCurrentView()!=usernameText)
             {
                 username_s.showPrevious();
                 userB.setBackgroundResource(R.drawable.ic_action_edit);

             }
             else{
                 username_s.showNext();
                 userB.setBackgroundResource(R.drawable.ic_action_edit_on);
             }
            }
        });
        emailB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        nameB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        surnameB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        //Permissões de acesso a storage e Upload de imagem
        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Verifica se a versão é superior à Marshmallow
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //Verifica permissão de leitura
                    if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //Aviso - Permissão negada
                        Toast.makeText(ProfileActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
                        //Pedido de permissão
                        ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        //Aviso - Permissão já concedida
                        Toast.makeText(ProfileActivity.this, "You already have permission", Toast.LENGTH_LONG).show();
                    }
                    //Verifica se tem permiissão de escrita
                    if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //Aviso - Permissão negada
                        Toast.makeText(ProfileActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
                        //Pedido de permissão
                        ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    } else {
                        //Escolha e imagem a fazer upload
                         chooseImage();
                         uploadImage();
                    }
                }
            }
        });
    }

    private void setUserData()
    {
        currentUser = mAuth.getCurrentUser().getUid().toString();
        if(nameEdit !=null)
        {
            mDatabase.child(currentUser).child("name").setValue(nameEdit.getText().toString());
        }
        if(emailEdit !=null)
        {
            mDatabase.child(currentUser).child("email").setValue(emailEdit.getText().toString());

        }
        if(usernameEdit !=null)
        {
            mDatabase.child(currentUser).child("username").setValue(usernameEdit.getText().toString());

        }
        if(surnameEdit !=null)
        {
            mDatabase.child(currentUser).child("surname").setValue(surnameEdit.getText().toString());
        }

        /*
        Alteração da palavra passe no FirebaseAuth e na Base de dados
        Verificação se os campos foram preenchidos

        FAZER FUNÇÃO NO BUTÃO DE SAVE SETTINGS VERIFICAR POR EMAIL E PASSWORD!!!!
         */
        if(oldEdit!=null && passEdit!=null &&
                confirmEdit!=null &&  passEdit.length()>=6 &&
                passEdit.getText().toString().equals(confirmEdit.getText().toString()))
        {
            final String email = mAuth.getCurrentUser().getEmail().toString();
            final String oldpass = oldEdit.getText().toString();
            //Progresso de alteração de palavra passe
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Authenticating...");
            progressDialog.show();

            AuthCredential credential = EmailAuthProvider.getCredential(email,oldpass);
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                 if(task.isSuccessful())
                 {
                     //Alteração da palavra passe
                     user.updatePassword(passEdit.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                         @Override
                         public void onComplete(@NonNull Task<Void> task) {
                             //Sucesso
                             if(!task.isSuccessful())
                             {
                                 progressDialog.dismiss();
                                 Toast.makeText(ProfileActivity.this, "Something went wrong. Please try again", Toast.LENGTH_SHORT).show();
                             }
                             //Falha
                             else {
                                 progressDialog.dismiss();
                                 Toast.makeText(ProfileActivity.this, "Password Successfully Modified", Toast.LENGTH_SHORT).show();

                             }
                         }
                     });
                 }
                 //Autenticação falhada
                 else {
                     progressDialog.dismiss();
                     Toast.makeText(ProfileActivity.this, "Authentication Failed. Please try again", Toast.LENGTH_SHORT).show();
                 }
                }
            });
            mDatabase.child(currentUser).child("surname").setValue(surnameEdit.getText().toString());

        }

    }

    private void setProfilePicture()
    {

    }

    private void getUserData(){
      currentUser = mAuth.getCurrentUser().getUid().toString();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                if(dataSnapshot.hasChild(currentUser))
                {
                    String username,email,name,surname,url;
                    username = postSnapshot.child("username").getValue().toString();
                    email= postSnapshot.child("email").getValue().toString();
                    name = postSnapshot.child("name").getValue().toString();
                    surname = postSnapshot.child("surname").getValue().toString();
                    url = postSnapshot.child("url").getValue().toString();
                    if(username==null || email==null || name==null || surname == null)
                    {
                        sendtoLogin();
                    }
                    else
                    {
                        updateUI(username,email,name,surname);
                        //Atualizar imagem aqui =)
                    }
                }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG,"Entrou 4");
            }
        });
    }



    //Atualização dos dados do utilizador nos campos a editar
    private void updateUI(String username, String email, String name, String surname)
    {
        this.nameText.setHint(name);
        this.usernameText.setHint(username);
        this.emailText.setHint(email);
        this.surnameText.setHint(surname);
    }
    //Criação da atividade para escolher a imagem
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
    }
    //Gestão do resultado da atividade
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                //Atualiza a foto de perfil na app
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                setupImage.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    //Upload de Imagem para a FireStorage
    private void uploadImage() {
        //Verifica se o ficheiro foi escolhido
        if(filePath != null)
        {
            //Inicialização de caixa de progresso de upload
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            /*
            Incio do upload
            Imagem é guardada na pasta images com o nome (UserID.jpg)
            Após a tarefa ser concluida (com ou sem sucessso) a caixa de progresso é dispensada
             */
            StorageReference ref = mStorageRef.child("images/"+currentUser+".jpg" );
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    //Click no hamburguer menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //Retorna o utilizador para o login
    private void sendtoLogin() {
        Intent intent = new Intent(ProfileActivity.this,LoginActivity.class);
        startActivity(intent);
    }
}
