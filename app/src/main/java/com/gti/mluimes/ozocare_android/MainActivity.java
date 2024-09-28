package com.gti.mluimes.ozocare_android;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String ETIQUETA_LOG = ">>>>";
    private static final int CODIGO_PETICION_PERMISOS = 11223344;

    private BluetoothLeScanner elEscanner;
    private ScanCallback callbackDelEscaneo = null;
    private Set<String> dispositivosEscaneados = new HashSet<>(); // Para evitar duplicados

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(ETIQUETA_LOG, "onCreate(): empieza");
        inicializarBluetooth();
        Log.d(ETIQUETA_LOG, "onCreate(): termina");
    }

    private void inicializarBluetooth() {
        Log.d(ETIQUETA_LOG, "inicializarBluetooth(): obteniendo adaptador BT");

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        // Verificar si el dispositivo soporta Bluetooth
        if (bta == null) {
            Log.d(ETIQUETA_LOG, "inicializarBluetooth(): El dispositivo no soporta Bluetooth.");
            return;
        }

        verificarPermisos();

        // Si Bluetooth no está habilitado, habilitarlo
        if (!bta.isEnabled()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                bta.enable();  // Esta es la llamada que requiere permisos.
                Log.d(ETIQUETA_LOG, "inicializarBluetooth(): Bluetooth habilitado.");
            } else {
                Log.d(ETIQUETA_LOG, "inicializarBluetooth(): Permiso de Bluetooth no concedido.");
            }
        } else {
            Log.d(ETIQUETA_LOG, "inicializarBluetooth(): Bluetooth ya estaba habilitado.");
        }

        // Obtenemos el escáner Bluetooth LE
        this.elEscanner = bta.getBluetoothLeScanner();

        if (this.elEscanner == null) {
            Log.d(ETIQUETA_LOG, "inicializarBluetooth(): No se ha podido obtener el escáner BTLE.");
        } else {
            Log.d(ETIQUETA_LOG, "inicializarBluetooth(): Escáner BTLE obtenido correctamente.");
        }
    }

    private void verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_SCAN)) {
                // Muestra un mensaje explicativo al usuario
                Toast.makeText(this, "Necesitamos permisos para escanear dispositivos Bluetooth", Toast.LENGTH_LONG).show();
            }

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION}, CODIGO_PETICION_PERMISOS);
        } else {
            Log.d(ETIQUETA_LOG, "verificarPermisos(): Todos los permisos están concedidos.");
        }
    }

    private void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, "buscarTodosLosDispositivosBTLE(): empieza");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                mostrarInformacionDispositivoBTLE(resultado);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                for (ScanResult resultado : results) {
                    mostrarInformacionDispositivoBTLE(resultado);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, "onScanFailed(): error code = " + errorCode);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            this.elEscanner.startScan(this.callbackDelEscaneo);
        } else {
            verificarPermisos();
        }
    }

    private void mostrarInformacionDispositivoBTLE(ScanResult resultado) {
        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord() != null ? resultado.getScanRecord().getBytes() : null;
        int rssi = resultado.getRssi();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            verificarPermisos();
            return;
        }

        if (dispositivosEscaneados.add(bluetoothDevice.getAddress())) { // Solo registra si no se ha visto antes
            Log.d(ETIQUETA_LOG, "nombre = " + bluetoothDevice.getName());
            Log.d(ETIQUETA_LOG, "dirección = " + bluetoothDevice.getAddress());
            Log.d(ETIQUETA_LOG, "rssi = " + rssi);
            if (bytes != null) {
                Log.d(ETIQUETA_LOG, "bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));
            }

            // Mostrar detalles de iBeacon (si lo hay)
            TramaIBeacon tib = new TramaIBeacon(bytes);
            Log.d(ETIQUETA_LOG, "uuid = " + Utilidades.bytesToString(tib.getUUID()));
        } else {
            Log.d(ETIQUETA_LOG, "Dispositivo ya escaneado: " + bluetoothDevice.getAddress());
        }
    }

    private void detenerBusquedaDispositivosBTLE() {
        if (this.callbackDelEscaneo != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                this.elEscanner.stopScan(this.callbackDelEscaneo);
                this.callbackDelEscaneo = null;
                Log.d(ETIQUETA_LOG, "Búsqueda de dispositivos detenida.");
            } else {
                Log.d(ETIQUETA_LOG, "detenerBusquedaDispositivosBTLE(): Permiso de escaneo no concedido.");
            }
        }
    }

    public void botonBuscarDispositivosBTLEPulsado(View v) {
        buscarTodosLosDispositivosBTLE();
    }

    public void botonDetenerBusquedaDispositivosBTLEPulsado(View v) {
        detenerBusquedaDispositivosBTLE();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CODIGO_PETICION_PERMISOS) {
            boolean todosLosPermisosConcedidos = true;
            for (int resultado : grantResults) {
                if (resultado != PackageManager.PERMISSION_GRANTED) {
                    todosLosPermisosConcedidos = false;
                    break;
                }
            }

            if (todosLosPermisosConcedidos) {
                Log.d(ETIQUETA_LOG, "onRequestPermissionsResult(): permisos concedidos!");
            } else {
                Log.d(ETIQUETA_LOG, "onRequestPermissionsResult(): permisos NO concedidos.");
                Toast.makeText(this, "Los permisos son necesarios para el funcionamiento de la aplicación.", Toast.LENGTH_LONG).show();
            }
        }
    }
}