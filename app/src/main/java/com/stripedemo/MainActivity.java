package com.stripedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {
    private String cardNumber;
    private String cardCVC;
    private int cardExpMonth;
    private int cardExpYear;
    private EditText editCardNumber, editEpiryDate, editRefDesc, editEmail, editCvcNumber;
    String userDescription, userEmail;
    String publishableKey = "pk_test_vtCsCOaKtOgyEfwjWaxTBy2k";
    Button btnChargeCard;
    Spinner spinnerCurrency;
    final String[] choices = {"USD", "INR"};
    boolean isLive = true;
    private android.support.v7.widget.SwitchCompat switchLive;
    private EditText editAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        switchLive = (SwitchCompat) findViewById(R.id.switchLive);
        editAmount = (EditText) findViewById(R.id.editAmount);
        editCardNumber = (EditText) findViewById(R.id.editCardNumber);
        editEpiryDate = (EditText) findViewById(R.id.editEpiryDate);
        editRefDesc = (EditText) findViewById(R.id.editRefDesc);
        editCvcNumber = (EditText) findViewById(R.id.editCvcNumber);
        editEmail = (EditText) findViewById(R.id.editEmail);
        btnChargeCard = (Button) findViewById(R.id.btnChargeCard);
        spinnerCurrency = (Spinner) findViewById(R.id.spinnerCurrency);
        editEpiryDate.addTextChangedListener(tw);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, choices);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(arrayAdapter);

        btnChargeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDataValid())
                    if (isCardValid()) {
                        call();
                    }
            }
        });
        switchLive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    isLive = true;
                } else
                    isLive = false;
            }
        });

    }

    TextWatcher tw = new TextWatcher() {
        private String current = "";
        private String mmyy = "MMYY";
        private Calendar cal = Calendar.getInstance();

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!s.toString().equals(current)) {
                String clean = s.toString().replaceAll("[^\\d.]", "");
                String cleanC = current.replaceAll("[^\\d.]", "");

                int cl = clean.length();
                int sel = cl;
                for (int i = 2; i <= cl && i < 4; i += 2) {
                    sel++;
                }
                //Fix for pressing delete next to a forward slash
                if (clean.equals(cleanC)) sel--;

                if (clean.length() < 4) {
                    clean = clean + mmyy.substring(clean.length());
                } else {
                    //This part makes sure that when we finish entering numbers
                    //the date is correct, fixing it otherwise
                    int mon = Integer.parseInt(clean.substring(0, 2));
                    int year = Integer.parseInt(clean.substring(2, 4));


                    if (mon > 12) mon = 12;
                    cal.set(Calendar.MONTH, mon - 1);
                    // year = (year<19)?19:(year>21)?21:year;
                    // cal.set(Calendar.YEAR, year);
                    // ^ first set year for the line below to work correctly
                    //with leap years - otherwise, date e.g. 29/02/2012
                    //would be automatically corrected to 28/02/2012

                    //  day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                    clean = String.format("%02d%02d", mon, year);
                }

                clean = String.format("%s/%s", clean.substring(0, 2),
                        clean.substring(2, 4));

                sel = sel < 0 ? 0 : sel;
                current = clean;
                editEpiryDate.setText(current);
                editEpiryDate.setSelection(sel < current.length() ? sel : current.length());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private boolean isDataValid() {
        if (editAmount.getText().toString().trim().equals("")) {
            editAmount.requestFocus();
            editAmount.setError("Amount cannot be empty");
            return false;
        } else if (editCardNumber.getText().toString().trim().equals("")) {
            editCardNumber.requestFocus();
            editCardNumber.setError("Card number cannot be empty.");
            return false;
        } else if (editCardNumber.getText().toString().length() < 14) {
            editCardNumber.requestFocus();
            editCardNumber.setError("Card number should be more than eleven character.");
            return false;
        } else if (editEpiryDate.getText().toString().trim().equals("")) {
            editEpiryDate.requestFocus();
            editEpiryDate.setError("Expiry date cannot be empty.");
            return false;
        } else if (editCvcNumber.getText().toString().trim().equals("")) {
            editCvcNumber.requestFocus();
            editCvcNumber.setError("Card CVC number cannot be empty.");
            return false;
        } else if (editEmail.getText().toString().trim().equals("")) {
            editEmail.requestFocus();
            editEmail.setError("E-mail cannot be empty.");
            return false;
        } else if (!isValidEmail(editEmail.getText().toString().trim())) {
            editEmail.requestFocus();
            editEmail.setError("Please enter valid email address.");
            return false;
        } else {
            return true;
        }
    }


    private boolean isCardValid() {
        cardNumber = editCardNumber.getText().toString().replace(" ", "");
        cardCVC = editCvcNumber.getText().toString();
        StringTokenizer tokens;
        if (!editEpiryDate.getText().toString().equalsIgnoreCase(""))
            tokens = new StringTokenizer(editEpiryDate.getText().toString(), "/");
        else {

            Toast.makeText(MainActivity.this, "The expiration date is blank.", Toast.LENGTH_SHORT).show();
            return false;
        }
        cardExpMonth = Integer.parseInt(tokens.nextToken());
        cardExpYear = Integer.parseInt(tokens.nextToken());
        Card card = new Card(cardNumber, cardExpMonth, cardExpYear, cardCVC);
        boolean validation = card.validateCard();
        if (validation) {
            return true;
        } else if (!card.validateNumber()) {
            Toast.makeText(MainActivity.this, "The card number that you entered is invalid.", Toast.LENGTH_SHORT).show();

        } else if (!card.validateExpiryDate()) {
            Toast.makeText(MainActivity.this, "The expiration date that you entered is invalid.", Toast.LENGTH_SHORT).show();
        } else if (!card.validateCVC()) {
            Toast.makeText(MainActivity.this, "The CVC code that you entered is invalid.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "The card details that you entered are invalid.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    public void call() {
        Card card = new Card(cardNumber, cardExpMonth, cardExpYear, cardCVC);
        try {
            Stripe stripe = new Stripe(publishableKey);
            stripe.createToken(
                    card,
                    publishableKey,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            sendCardToInfoToStripe(token.getId(), isLive);
                        }

                        public void onError(Exception error) {
                            Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (Exception e) {

        }
    }

    private void sendCardToInfoToStripe(final String cardToken, final boolean isLive) {
        userDescription = editRefDesc.getText().toString(); // Get User Description
        userEmail = editEmail.getText().toString(); // Get User Email Id
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> chargeParams = new HashMap<String, Object>();
                chargeParams.put("amount", 1000);
                chargeParams.put("currency", spinnerCurrency.getSelectedItem());
                chargeParams.put("source", cardToken);
                chargeParams.put("description", userDescription);
                chargeParams.put("receipt_email", userEmail);
                chargeParams.put("application_fee", 100);
                RequestOptions requestOptions;
                requestOptions = RequestOptions.builder().setStripeAccount("acct_18fSBSH7WJ0jBqkP").setApiKey("sk_test_5ScO0y895ylFb0CHIYYNZDrL").build();
                try {
                    Charge.create(chargeParams, requestOptions);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Your Payment has been done successfully!!", Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            }
        }).start();

    }

    private boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return target != null;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
