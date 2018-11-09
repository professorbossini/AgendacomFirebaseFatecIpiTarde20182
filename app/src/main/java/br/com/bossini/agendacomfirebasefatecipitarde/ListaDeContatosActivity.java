package br.com.bossini.agendacomfirebasefatecipitarde;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

public class ListaDeContatosActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference contatosReference;
    private void configuraFirebase (){
        firebaseDatabase = FirebaseDatabase.getInstance();
        contatosReference = firebaseDatabase.getReference("contatos");
    }

    @Override
    protected void onStart() {
        super.onStart();
        contatosReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contatos.clear();
                for (DataSnapshot filho : dataSnapshot.getChildren()){
                    Contato contato = filho.getValue(Contato.class);
                    contato.setId(filho.getKey());
                    contatos.add(contato);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private List <Contato> contatos;
    private ArrayAdapter <Contato> adapter;
    private ListView contatosListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_de_contatos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        contatos = new LinkedList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, contatos);
        contatosListView = findViewById(R.id.contatosListView);
        configuraObserverLongClick();
        contatosListView.setAdapter(adapter);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListaDeContatosActivity.this,
                                                AdicionaContatoActivity.class);
                startActivity(intent);
            }
        });
        configuraFirebase();
    }

    private void configuraObserverLongClick (){
        contatosListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
                AlertDialog.Builder alertBuilder =
                        new AlertDialog.Builder(ListaDeContatosActivity.this);
                AlertDialog dialogo = alertBuilder.setNegativeButton(
                        R.string.atualizar_contato, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder atualizarDialogoBuilder = new
                                        AlertDialog.Builder(ListaDeContatosActivity.this);
                                View contatoView = LayoutInflater.from(
                                        ListaDeContatosActivity.this).
                                        inflate(R.layout.activity_adiciona_contato, null);
                                atualizarDialogoBuilder.setView(contatoView);
                                final EditText nomeEditText =
                                        contatoView.findViewById(R.id.nomeEditText);
                                final EditText foneEditText =
                                        contatoView.findViewById(R.id.foneEditText);
                                final EditText emailEditText =
                                        contatoView.findViewById(R.id.emailEditText);
                                final Contato contato = contatos.get(position);
                                nomeEditText.setText(contato.getNome());
                                foneEditText.setText(contato.getFone());
                                emailEditText.setText(contato.getEmail());
                                FloatingActionButton floatingActionButton =
                                        contatoView.findViewById(R.id.fab);
                                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String nome = nomeEditText.getEditableText().toString();
                                        String fone = foneEditText.getEditableText().toString();
                                        String email = emailEditText.getEditableText().toString();
                                        Contato contatoAtualizar = new Contato(nome, fone, email);
                                        contatosReference.
                                                child(contato.getId()).
                                                setValue(contatoAtualizar);
                                        Toast.makeText(ListaDeContatosActivity.this,
                                                getString(R.string.atualizar_contato),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                                atualizarDialogoBuilder.setView(contatoView);
                                atualizarDialogoBuilder.create().show();
                            }
                        }).setPositiveButton(
                        R.string.deletar_contato, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Contato contato = contatos.get(position);
                                contatosReference.child(contato.getId()).removeValue();
                            }
                        }).create();
                dialogo.show();
                return false;
            }
        });
    }
}
