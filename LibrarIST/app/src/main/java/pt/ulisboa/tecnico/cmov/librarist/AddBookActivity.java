package pt.ulisboa.tecnico.cmov.librarist;

import static pt.ulisboa.tecnico.cmov.librarist.AddLibraryActivity.drawableToBitmap;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Objects;

public class AddBookActivity extends AppCompatActivity {
    ActivityResultLauncher<String> coverImageLauncher;
    boolean notifications = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        ImageView bookCoverImage = findViewById(R.id.bookcover);
        coverImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        bookCoverImage.setImageURI(result);
                    }
                }
        );

        Button addCoverBtn = findViewById(R.id.addcover);
        addCoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coverImageLauncher.launch("image/*");
            }
        });

        TextInputEditText bookTitle = findViewById(R.id.booktitle);

        TextInputEditText bookBarcode = findViewById(R.id.bookbarcode);
        bookBarcode.setText(getIntent().getSerializableExtra("BARCODE").toString());

        SwitchCompat notificationsSwitch = findViewById(R.id.notifications);
        notificationsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifications = !notifications;
            }
        });

        Button addBookBtn = findViewById(R.id.addbook);
        addBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String barcode = Objects.requireNonNull(bookBarcode.getText()).toString();
                String title = Objects.requireNonNull(bookTitle.getText()).toString();
                Bitmap bitmap = drawableToBitmap(bookCoverImage.getDrawable());

                String cover = saveCover(title, bitmap);
                Book b = new Book(barcode,title, cover, notifications);

                Intent intent = new Intent();
                intent.putExtra("BOOK", b);
                setResult(3, intent);
                Toast.makeText(getApplicationContext(), "Adding Book...", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        Button cancelBtn = findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    public String saveCover(String fName, Bitmap bitmap){
        try {
            writeBitmapToFile(fName.replaceAll("\\s", "") + ".bmp", bitmap);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return getApplicationContext().getFilesDir().toString() + "/" + fName.replaceAll("\\s", "") + ".bmp";
    }
    private void writeBitmapToFile(String fileName, Bitmap content) throws FileNotFoundException {
        File path = getApplicationContext().getFilesDir();
        try{
            FileOutputStream writer = new FileOutputStream(new File(path, fileName));
            content.compress(Bitmap.CompressFormat.PNG,100,writer);
            Toast.makeText(this, "Wrote to file: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}