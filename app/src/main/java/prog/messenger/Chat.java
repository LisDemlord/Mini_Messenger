package prog.messenger;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.kgsu.network.TCPConnection;
import com.kgsu.network.TCPConnectionListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class Chat extends AppCompatActivity implements View.OnClickListener, TCPConnectionListener {

    // Создание логов
    private static final String TAG = "Logs";

    // Объявление элементов
    public static final String IP_ADDR = "192.168.0.4";
    public static final int PORT = 5999;
    private TCPConnection connection;
    EditText etIn;
    Button btnIn;
    TextView tvOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        // Нахождение элементов
        etIn = (EditText) findViewById(R.id.etIn);
        btnIn = (Button) findViewById(R.id.btnIn);
        tvOut = (TextView) findViewById(R.id.tvOut);

        // Присвоение обработчика кнопке
        btnIn.setOnClickListener(this);

        // Новый поток для создания подключения
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connection = new TCPConnection(Chat.this, IP_ADDR, PORT);
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                    Toast.makeText(Chat.this, "Невозможно подключиться к серверу", Toast.LENGTH_SHORT).show();
                    finish();
                }

            }
        }).start();
    }

    // Описание обработчика
    @Override
    public void onClick(View view) {
        String msg_0, msg_end;


        // Проверяем поля на пустоту
        if (TextUtils.isEmpty(etIn.getText().toString())) {
            Log.d(TAG, "Не введено сообщение;");
            Toast.makeText(this, "Сообщение не должно быть пустым", Toast.LENGTH_SHORT).show();
            return;
        }

        // Экспорт ника из главной активности
        Bundle arguments = getIntent().getExtras();
        String nick = arguments.getString("nick");

        // Чтение текущего сообщения
        msg_0 = etIn.getText().toString();
        Log.d(TAG, msg_0 + ";");

        // Проверка сообщения на наличие лишних пробелов
        if (msg_0.trim().length() > 0) {
            msg_end = nick + ": " + msg_0.trim();
            Log.d(TAG, msg_end + ";");

            // Отдельный поток для отправки сообщений
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connection.sendString(msg_end);
                }
            }).start();
        }
        else {
            Log.d(TAG, "Сообщение состоит из пробелов;");
            Toast.makeText(this, "Сообщение не должно состоять только из пробелов", Toast.LENGTH_SHORT).show();
        }

        etIn.setText("");
    }

    // Объявление меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mes_menu, menu);
        return true;
    }

    // Обработка нажатий в меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Проверка, какая именно кнопка была нажата
        switch (item.getItemId()) {
            case R.id.reset:
                // очищаем поле
                Log.d(TAG, "Нажато меню: reset;");
                tvOut.setText("");
                break;
            case R.id.quit:
                // выход из приложения
                Log.d(TAG, "Нажато меню: quit;");
                finish();
                break;
        }
        return true;
    }

    // Срабатывает при подключении к серверу
    @Override
    public void onConnection(TCPConnection tcpConnection) { }

    // Срабатывает при получении строки
    @Override
    public void onStringInput(TCPConnection tcpConnection, String value) {
        printMsg(value);
    }

    // Срабатывает при отключении от сервера
    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Chat.this, "Невозможно подключиться к серверу", Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }

    // Срабатывает при исключении
    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Chat.this, "Код ошибки " + e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }

    // Метод для вывода сообщений
    public void printMsg(String value) {
        // Поток для работы с интерфейсом UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!value.equals("null"))
                    tvOut.setText(tvOut.getText() + "\r\n" + value);
            }
        });
    }
}