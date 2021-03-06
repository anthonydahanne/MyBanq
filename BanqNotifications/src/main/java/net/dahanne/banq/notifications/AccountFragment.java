package net.dahanne.banq.notifications;

import android.accounts.Account;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.dahanne.banq.BanqClient;
import net.dahanne.banq.exceptions.InvalidSessionException;
import net.dahanne.banq.model.Details;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountFragment extends Fragment {
    private static final String EXTRA_ACOUNT_NAME = "EXTRA_ACCOUNT";
    private static final String EXTRA_ACOUNT_TYPE = "EXTRA_ACOUNT_TYPE";
    private TextView userName;
    private TextView currentDebt;
    private TextView debtToCome;
    private TextView reservationNumber;
    private Account account;
    private static Logger LOG = LoggerFactory.getLogger(BanqClient.class);


    public static AccountFragment newInstance(Account account) {
        AccountFragment accountFragment = new AccountFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ACOUNT_NAME, account.name);
        bundle.putString(EXTRA_ACOUNT_TYPE, account.type);
        accountFragment.setArguments(bundle);
        return accountFragment;
    }

    public String getTitle() {
        return getArguments().getString(EXTRA_ACOUNT_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout wrapper = new LinearLayout(getActivity());
        inflater.inflate(R.layout.fragment_main, wrapper, true);
        userName = (TextView) wrapper.findViewById(R.id.userName);
        currentDebt = (TextView) wrapper.findViewById(R.id.currentDebt);
        debtToCome = (TextView) wrapper.findViewById(R.id.debtToCome);
        reservationNumber = (TextView) wrapper.findViewById(R.id.reservationNumber);
        return wrapper;
    }


    @Override
    public void onStart() {
        super.onStart();
        account = new Account(getArguments().getString(EXTRA_ACOUNT_NAME), getArguments().getString(EXTRA_ACOUNT_TYPE));
    }

    @Override
    public void onResume() {
        super.onResume();

        // ouai ouai c'est pas super de tout recharger a chaque fois
        // mais si on fait pas ca, l utilisateur change la valeur de remaining days
        // et ca rafraichit pas; un mal pour 1 bien en attendant mieux
        refresh();
    }


    public void refresh() {
        new RetrieveInfosAsyncTask().execute();
    }


    class RetrieveInfosAsyncTask extends AsyncTask<Void, Void, Details> {
        private Exception exceptionCaught;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected Details doInBackground(Void[] nothing) {
            Details details = null;
            BanqClient bc = null;
            try {
                bc = Authenticator.getBanqClient(getActivity(), account);
                LOG.info("Getting details");
                details = bc.getDetails();
                LOG.info("Details retrieved");
            } catch (InvalidSessionException ise) {
                try {
                    LOG.info("Session not valid : trying to reconnect");
                    Authenticator.authenticate(getActivity(), account);
                    details = bc.getDetails();
                    LOG.info("Detail retrieved");
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    exceptionCaught = e;
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                exceptionCaught = e;
            }
            return details;
        }

        @Override
        protected void onPostExecute(Details details) {
            showProgress(false);
            if (exceptionCaught == null && details != null) {
                userName.setText(String.format(getString(R.string.name), details.getName()));
                Spannable debt = new SpannableString(String.format(getString(R.string.currentDebt), details.getCurrentDebt()));
                Spannable debtToComeSpanable = new SpannableString(String.format(getString(R.string.debtToCome), details.getLateFeesToCome()));
//                debt.setSpan(new ForegroundColorSpan(Color.RED), debt.length() - 5, debt.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                currentDebt.setText(debt);
                debtToCome.setText(debtToComeSpanable);
//                String formattedExpirationDate = DateFormat.getDateInstance().format(details.getExpirationDate());
//                Spannable expiration = new SpannableString(String.format(getString(R.string.expirationDebt), formattedExpirationDate));
//                expiration.setSpan(new ForegroundColorSpan(DateComparatorUtil.getExpirationColor(details.getRemaingDays())), expiration.length() - formattedExpirationDate.length(), expiration.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                expirationDate.setText(expiration);
                reservationNumber.setText(String.format(getString(R.string.reservation_number), details.getReservationsNumber()));
                if(details.getBorrowedItems().isEmpty()) {
                    TextView viewById = (TextView) getView().findViewById(R.id.no_items);
                    viewById.setText(getString(R.string.no_borrowed_items, details.getName()));
                    viewById.setVisibility(View.VISIBLE);
                } else {
                    ((GridView) getView().findViewById(android.R.id.list)).setAdapter(new BorrowedItemAdapter(getActivity(), details.getBorrowedItems(), account));
                }
            } else if (exceptionCaught == null) {
                Toast.makeText(getActivity(), getString(R.string.unexpectedError), Toast.LENGTH_SHORT).show();
            } else if (exceptionCaught instanceof InvalidSessionException) {
                Toast.makeText(getActivity(), getString(R.string.invalid_session), Toast.LENGTH_SHORT).show();
//                ((MainActivity) getActivity()).backToLogin();
            } else {
                Toast.makeText(getActivity(), exceptionCaught.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (getView() == null) {
            // got a NPE once because of getView()
            return;
        }
        final View detailStatusView = getView().findViewById(R.id.detail_status);
        final View userInfosView = getView().findViewById(R.id.user_infos);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            detailStatusView.setVisibility(View.VISIBLE);
            detailStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            detailStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            userInfosView.setVisibility(View.VISIBLE);
            userInfosView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            userInfosView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            detailStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            userInfosView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}