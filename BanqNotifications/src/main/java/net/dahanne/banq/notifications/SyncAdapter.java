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
import java.util.ArrayList;
import java.util.List;

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
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        try {
            Log.i(getClass().getSimpleName(), "Start syncing");
            BanqClient bc = Authenticator.getBanqClient(mContext, account);
            Log.i(getClass().getSimpleName(), "Getting details");
            Details details = getDetails(bc, account);
            Log.i(getClass().getSimpleName(), "Detail retrieved");
            List<BorrowedItem> itemsToReturnSoon = new ArrayList<BorrowedItem>();
            for (BorrowedItem borrowedItem : details.getBorrowedItems()) {
                if (DateComparatorUtil.shouldPopNotification(mContext, borrowedItem.getRemainingDays())) {
                    NotificationHelper.launchNotification(mContext, borrowedItem.getTitle(), borrowedItem.getRemainingDays());
                    itemsToReturnSoon.add(borrowedItem);
                }
            }
            if (!itemsToReturnSoon.isEmpty()) {
                if (itemsToReturnSoon.size() == 1) {
                    NotificationHelper.launchNotification(mContext, itemsToReturnSoon.get(0).getTitle(), itemsToReturnSoon.get(0).getRemainingDays());
                } else {
                    long shorterDelayToReturn = 100;
                    for (BorrowedItem borrowedItem : itemsToReturnSoon) {
                        shorterDelayToReturn = borrowedItem.getRemainingDays() < shorterDelayToReturn ? borrowedItem.getRemainingDays() : shorterDelayToReturn;
                    }
                    NotificationHelper.launchNotification(mContext, mContext.getString(R.string.several_items_to_return_soon), shorterDelayToReturn);
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

    private Details getDetails(BanqClient bc, Account account) throws Exception {
        try {
            return bc.getDetails();
        } catch (InvalidSessionException ise) {
            Authenticator.authenticate(mContext, account);
            return bc.getDetails();
        }
    }
}

