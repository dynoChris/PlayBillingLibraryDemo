package com.oliverstudio.playbillinglibrarydemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button mBuyButton;

    private BillingClient mBillingClient;
    private Map<String, SkuDetails> mSkuDetailsMap = new HashMap<>();

    private String mSkuId = "toast_hello_world";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBilling();

        mBuyButton = findViewById(R.id.buy_btn);
        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchBilling(mSkuId);
            }
        });
    }

    private void initBilling() {
        mBillingClient = BillingClient.newBuilder(this).setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
                if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
                    //here when purchase completed
                    payComplete();
                }
            }
        }).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    //below you can query information about products and purchase
                    querySkuDetails(); //query for products
                    List<Purchase> purchasesList = queryPurchases(); //query for purchases

                    //if the purchase has already been made to give the goods
                    for (int i = 0; i < purchasesList.size(); i++) {
                        String purchaseId = purchasesList.get(i).getSku();
                        if(TextUtils.equals(mSkuId, purchaseId)) {
                            payComplete();
                        }
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                //here when something went wrong, e.g. no internet connection
            }
        });
    }

    private void querySkuDetails() {
        SkuDetailsParams.Builder skuDetailsParamsBuilder = SkuDetailsParams.newBuilder();
        List<String> skuList = new ArrayList<>();
        skuList.add(mSkuId);
        skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        mBillingClient.querySkuDetailsAsync(skuDetailsParamsBuilder.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                if (responseCode == 0) {
                    for (SkuDetails skuDetails : skuDetailsList) {
                        mSkuDetailsMap.put(skuDetails.getSku(), skuDetails);
                    }
                }
            }
        });
    }

    private List<Purchase> queryPurchases() {
        Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
        return purchasesResult.getPurchasesList();
    }

    public void launchBilling(String skuId) {
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(mSkuDetailsMap.get(skuId))
                .build();
        mBillingClient.launchBillingFlow(this, billingFlowParams);
    }

    private void payComplete() {
        Toast.makeText(this, getString(R.string.hello_world), Toast.LENGTH_SHORT).show();
    }
}