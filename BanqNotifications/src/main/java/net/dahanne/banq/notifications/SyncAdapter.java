/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.dahanne.banq.notifications;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import net.dahanne.banq.BanqClient;
import net.dahanne.banq.exceptions.InvalidSessionException;
import net.dahanne.banq.model.BorrowedItem;
import net.dahanne.banq.model.Details;

import java.io.IOException;
import java.util.Set;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.  This sample shows a basic 2-way
 * sync between the client and a sample server.  It also contains an
 * example of how to update the contacts' status messages, which
 * would be useful for a messaging or social networking client.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";

    private final Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        try {
            Log.i(getClass().getSimpleName(), "Start syncing");
            Set<String> cookies = Authenticator.getCookies(mContext, account);
            Log.i(getClass().getSimpleName(), "Cookies retrieved");
            BanqClient bc = new BanqClient();
            Details details = getDetails(cookies, bc, null);
            Log.i(getClass().getSimpleName(), "Detail retrieved");
            for (BorrowedItem borrowedItem : details.getBorrowedItems()) {
                if(DateComparatorUtil.shouldPopNotification(mContext, borrowedItem.getRemainingDays())) {
                    NotificationHelper.launchNotification(mContext, borrowedItem);
                }
            }
            Log.i(getClass().getSimpleName(), "Stop syncing");
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
            syncResult.stats.numIoExceptions++;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private Details getDetails(Set<String> cookies, BanqClient bc, Account account) throws java.text.ParseException, IOException, InterruptedException, InvalidSessionException {
        try {
            return bc.getDetails(cookies);
        } catch (InvalidSessionException ise) {
            cookies = Authenticator.authenticate(mContext, account, AccountManager.get(mContext).getPassword(account));
            return bc.getDetails(cookies);
        }
    }
}

