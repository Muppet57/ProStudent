package com.student.pro.prostudent;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    //Toolbar e Nav
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    // Elemento Imagem
    private CircleImageView setupImage;
    // Caminho Imagem
    private Uri filePath = null,imageURL=null;
    //Codigo de atividade Load imagem
    private static int RESULT_LOAD_IMAGE = 1;
    //Firebase
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private String TAG="profileteste", UserID;
    //TextView
    private TextView nameText, emailText, usernameText, surnameText;
    //EditText
    private EditText nameEdit, emailEdit, usernameEdit, surnameEdit, passEdit, confirmEdit;
    //ViewSwitcher
    private ViewSwitcher email_s,username_s,name_s,surname_s;
    //Buttons
    private Button emailB,userB,nameB,surnameB,save_settings;
    //ProgressBar
    private ProgressBar profile_bar;
    private Boolean upload_needed = false;

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
        this.save_settings=findViewById(R.id.profile_save);
        //Imagem de Perfil default
        this.setupImage = findViewById(R.id.setup_image);
        //progressbar
        this.profile_bar=findViewById(R.id.profile_bar);

        //Base de dados
        this.mDatabase = FirebaseDatabase.getInstance().getReference("users");
        //Storage
        this.mStorageRef = FirebaseStorage.getInstance().getReference();
        //Toolbar & Navbar
        this.mToolbar = findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);
        this.mDrawerLayout = findViewById(R.id.drawerlayout);
        this.mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        this.mDrawerLayout.addDrawerListener(mToggle);
        this.mToggle.syncState();
        getSupportActionBar().setTitle(R.string.title_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Update de informação do utilizador
        //Utilizadores
        this.mAuth = FirebaseAuth.getInstance();
        user= mAuth.getCurrentUser();
        UserID=user.getUid().toString();
        getUserData();

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
                if(email_s.getCurrentView()!=emailText)
                {
                    email_s.showPrevious();
                    emailB.setBackgroundResource(R.drawable.ic_action_edit);

                }
                else{
                    email_s.showNext();
                    emailB.setBackgroundResource(R.drawable.ic_action_edit_on);
                }
            }
        });
        nameB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(name_s.getCurrentView()!=nameText)
                {
                    name_s.showPrevious();
                    nameB.setBackgroundResource(R.drawable.ic_action_edit);

                }
                else{
                    name_s.showNext();
                    nameB.setBackgroundResource(R.drawable.ic_action_edit_on);
                }
            }
        });
        surnameB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(surname_s.getCurrentView()!=surnameText)
                {
                    surname_s.showPrevious();
                    surnameB.setBackgroundResource(R.drawable.ic_action_edit);

                }
                else{
                    surname_s.showNext();
                    surnameB.setBackgroundResource(R.drawable.ic_action_edit_on);
                }
            }
        });
        //Guardar alterações
        save_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile_bar.setVisibility(View.VISIBLE);
                if(!emailEdit.getText().toString().isEmpty() || !passEdit.getText().toString().isEmpty())
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setTitle(R.string.prompt_old_pass);

                    final EditText input = new EditText(ProfileActivity.this);
                    input.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
                    input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    builder.setView(input);
                    //cria Dialogo para introduzir palavra passe
                    builder.setPositiveButton(R.string.action_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(emailEdit.getText()!=null && passEdit.getText()!=null )
                            {
                               String m_Text = input.getText().toString();
                               setUserData(m_Text);
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
                else
                {
                    setUserData("");

                }
            }
        });

        /*
        Permissões de acesso a storage
        Seleção de Imagem
        Upload de imagem
         */
        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Verifica se a versão é superior à Marshmallow
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    //Verifica permissão de leitura
                    if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //Aviso - Permissão negada
                        Toast.makeText(ProfileActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
                        //Pedido de permissão
                        ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        //Aviso - Permissão já concedida
                        chooseImage();
                        Log.d(TAG, "onClick: ENTROU NO PRIMEIRO");
                    }

                }
                //Se for inferior (Para versões inferiores a autorização é pedida na play store)
                else
                {
                    chooseImage();
                    Log.d(TAG, "onClick: ENTROU NO SEGUNDO");

                }
            }
        });
    }



    private void setUserData(String oldpass)
    {
           /*
        Alteração da palavra passe ou Email no FirebaseAuth
        É necessário introduzir palavra passe para fazer alterações no FirabaseAuth
        Após feitas as alterações com sucesso no FirebaseAuth, são feitas as alterações na Base de dados
        A palavra passe não é guardada na base de dados
         */
        if(!passEdit.getText().toString().isEmpty() && !confirmEdit.getText().toString().isEmpty() &&  passEdit.length()>=6 && passEdit.getText().toString().equals(confirmEdit.getText().toString()))
        {
            Log.d(TAG, "setUserData: ALTERAÇÃO DE PASS");

            final String email = user.getEmail().toString();
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
                                    Toast.makeText(ProfileActivity.this, "Something went wrong. Your password was not updated. Please try again", Toast.LENGTH_SHORT).show();
                                }
                                //Falha
                                else {
                                    progressDialog.dismiss();
                                    Toast.makeText(ProfileActivity.this, "Password Successfully updated", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    }
                    //Autenticação falhada
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Authentication Failed. Your password was not updated", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if(!emailEdit.getText().toString().isEmpty())
        {
            Log.d(TAG, "setUserData: Alteração de email");
            final String email = user.getEmail().toString();
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
                        user.updateEmail(emailEdit.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //Sucesso
                                if(!task.isSuccessful())
                                {
                                    progressDialog.dismiss();
                                    Toast.makeText(ProfileActivity.this, "Something went wrong. Your email was not updated. Please try again", Toast.LENGTH_SHORT).show();
                                }
                                //Falha
                                else {
                                    progressDialog.dismiss();
                                    mDatabase.child(UserID).child("email").setValue(emailEdit.getText().toString());

                                    Toast.makeText(ProfileActivity.this, "Email Successfully updated", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    //Autenticação falhada
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Authentication Failed. Your email was not updated", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if(!nameEdit.getText().toString().isEmpty())
        {
            Log.d(TAG, "setUserData: guarda nome");
            mDatabase.child(UserID).child("name").setValue(nameEdit.getText().toString());
        }
        if(!usernameEdit.getText().toString().isEmpty())
        {
            Log.d(TAG, "setUserData: guarda username");
            mDatabase.child(UserID).child("username").setValue(usernameEdit.getText().toString());
        }
        if(!surnameEdit.getText().toString().isEmpty())
        {
            Log.d(TAG, "setUserData: guarda surname");
            mDatabase.child(UserID).child("surname").setValue(surnameEdit.getText().toString());
        }
        if(imageURL!=null)
        {
            Log.d(TAG, "setUserData: guarda imagem url");
            mDatabase.child(UserID).child("url").setValue(imageURL.toString());
        }
        //-------------------------------------------------------WARNING-------------------
        if(upload_needed==true)
        {
            uploadImage();
        }
        //-------------------------------------------------------WARNING-------------------
        else{
            profile_bar.setVisibility(View.INVISIBLE);
            sendtoHome();
        }
        profile_bar.setVisibility(View.INVISIBLE);
        sendtoHome();

    }

    private void sendtoHome() {
        Intent intent = new Intent(ProfileActivity.this,HomeActivity.class);
        startActivity(intent);
    }


    private void getUserData()
    {
        profile_bar.setVisibility(View.VISIBLE);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                if(dataSnapshot.hasChild(UserID))
                {
                    String username,email,name,surname,url;
                    username = postSnapshot.child("username").getValue().toString();
                    email= postSnapshot.child("email").getValue().toString();
                    name = postSnapshot.child("name").getValue().toString();
                    surname = postSnapshot.child("surname").getValue().toString();
                    url = postSnapshot.child("url").getValue().toString();
                    updateUI(username,email,name,surname,url);
                }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }
    //Atualização dos dados do utilizador nos campos a editar
    private void updateUI(String username, String email, String name, String surname,String url)
    {
        this.nameText.setHint(name);
        this.usernameText.setHint(username);
        this.emailText.setHint(email);
        this.surnameText.setHint(surname);
        Picasso.get()
                .load(url)
                .placeholder(R.drawable.default_icon)
                .error(R.drawable.default_icon)
                .into(setupImage);
        profile_bar.setVisibility(View.INVISIBLE);
        Toast.makeText(ProfileActivity.this, "User profile Loaded", Toast.LENGTH_SHORT).show();
    }
    //Criação da atividade para escolher a imagem
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
        Log.d(TAG, "chooseImage: ESCOLHEU IMAGEM");
    }
    //Gestão do resultado da atividade
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            Log.d(TAG, "onActivityResult: RESULTADO IMAGEM");
            filePath = data.getData();
            try {
                //Atualiza a foto de perfil na app
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                setupImage.setImageBitmap(bitmap);
                upload_needed = true;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            Log.d(TAG, "onActivityResult: ANTES DO UPLOAD");
            //uploadImage();
        }
    }
    //Upload de Imagem para a FireStorage
    private void uploadImage() {
        Log.d(TAG, "uploadImage: UPLOAD IMAGE");
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
            StorageReference ref = mStorageRef.child("images/"+UserID+".jpg" );
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageURL = taskSnapshot.getDownloadUrl();
                            Toast.makeText(ProfileActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Upload Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        upload_needed=false;
        Log.d(TAG, "uploadImage: DEPOIS UPLOAD");

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
    private void sendtoTickets(){
        Intent intent = new Intent(ProfileActivity.this,TicketActivity.class);
        startActivity(intent);
    }
    private void sendtoClass(){
        Intent intent = new Intent(ProfileActivity.this,ClassActivity.class);
        startActivity(intent);
    }
}
