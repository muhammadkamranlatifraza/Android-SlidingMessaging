package com.klinker.android.messaging_sliding.batch_delete;

import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_donate.utils.SendUtil;
import com.klinker.android.messaging_sliding.ImageViewer;
import com.klinker.android.messaging_sliding.MessageCursorAdapter;

import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BatchDeleteConversationArrayAdapter extends CursorAdapter {

    public static ArrayList<Long> itemsToDelete = new ArrayList<Long>();
    public static boolean checkedAll = false;

    private final Activity context;
    private final String myId;
    private final String inboxNumbers;
    private final String threadIds;
    private final Bitmap contactImage;
    private final Bitmap myImage;
    private SharedPreferences sharedPrefs;
    private ContentResolver contentResolver;
    private Cursor mCursor;
    private Paint paint;
    private Typeface font;
    private final LayoutInflater mInflater;

    // shared prefs values

    public final boolean darkContactImage;
    public final boolean customFont;
    public final boolean showOriginalTimestamp;
    public final boolean deliveryReports;
    public final boolean hourFormat;
    public final boolean stripUnicode;
    public final boolean contactPictures;
    public final boolean tinyDate;
    public final boolean customTheme;
    public final boolean emojiType;
    public final boolean smiliesType;
    public final String textSize;
    public final String runAs;
    public final String signature;
    public final String ringTone;
    public final String deliveryOptions;
    public final String sendingAnimation;
    public final String recieveAnimation;
    public final String themeName;
    public final String sentTextColor;
    public final String receivedTextColor;
    public final String textAlignment;
    public final String smilies;
    public final int ctRecievedTextColor;
    public final int ctSentTextColor;
    public final int ctConversationListBackground;
    public final int ctSentMessageBackground;
    public final int ctRecievedMessageBackground;
    public final int animationSpeed;
    public final int textOpacity;

    public BatchDeleteConversationArrayAdapter(Activity context, String myId, String inboxNumbers, String ids, Cursor query) {
        super(context, query, 0);
        this.context = context;
        this.myId = myId;
        this.inboxNumbers = inboxNumbers;
        this.threadIds = ids;
        this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.contentResolver = context.getContentResolver();
        this.mInflater = LayoutInflater.from(context);
        this.mCursor = query;

        Bitmap input;

        darkContactImage = sharedPrefs.getBoolean("ct_darkContactImage", false);
        showOriginalTimestamp = sharedPrefs.getBoolean("show_original_timestamp", false);
        deliveryReports = sharedPrefs.getBoolean("delivery_reports", false);
        hourFormat = sharedPrefs.getBoolean("hour_format", false);
        stripUnicode = sharedPrefs.getBoolean("strip_unicode", false);
        tinyDate = sharedPrefs.getBoolean("tiny_date", false);
        customTheme = sharedPrefs.getBoolean("custom_theme", false);
        emojiType = sharedPrefs.getBoolean("emoji_type", true);
        smiliesType = sharedPrefs.getBoolean("smiliesType", true);
        textSize = sharedPrefs.getString("text_size", "14");
        runAs = sharedPrefs.getString("run_as", "sliding");
        signature = sharedPrefs.getString("signature", "");
        ringTone = sharedPrefs.getString("ringtone", "null");
        deliveryOptions = sharedPrefs.getString("delivery_options", "2");
        sendingAnimation = sharedPrefs.getString("send_animation", "left");
        recieveAnimation = sharedPrefs.getString("receive_animation", "right");
        themeName = sharedPrefs.getString("ct_theme_name", "Light Theme");
        sentTextColor = sharedPrefs.getString("sent_text_color", "default");
        receivedTextColor = sharedPrefs.getString("received_text_color", "default");
        textAlignment = sharedPrefs.getString("text_alignment", "split");
        smilies = sharedPrefs.getString("smilies", "with");
        ctRecievedTextColor = sharedPrefs.getInt("ct_receivedTextColor", context.getResources().getColor(R.color.black));
        ctSentTextColor = sharedPrefs.getInt("ct_sentTextColor", context.getResources().getColor(R.color.black));
        ctConversationListBackground = sharedPrefs.getInt("ct_conversationListBackground", context.getResources().getColor(R.color.light_silver));
        ctSentMessageBackground = sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white));
        ctRecievedMessageBackground = sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white));
        animationSpeed = sharedPrefs.getInt("animation_speed", 300);
        textOpacity = sharedPrefs.getInt("text_opacity", 100);

        if (sharedPrefs.getBoolean("override_speed", false)) {
            contactPictures = false;
            customFont = false;
        } else {
            contactPictures = sharedPrefs.getBoolean("contact_pictures", true);
            customFont = sharedPrefs.getBoolean("custom_font", false);
        }

        Uri uri = Uri.parse("content://sms/conversations/" + threadIds);
        Cursor c = context.getContentResolver().query(uri, null, null, null, "date DESC");

        if (c.moveToFirst()) {
            do {
                if (c.getString(c.getColumnIndex("type")).equals("1")) {
                    inboxNumbers = c.getString(c.getColumnIndex("address"));
                    break;
                }
            } while (c.moveToNext());
        }

        c.close();

        try {
            input = ContactUtil.getFacebookPhoto(inboxNumbers, context);
        } catch (NumberFormatException e) {
            input = null;
        }

        if (input == null) {
            if (darkContactImage) {
                input = ContactUtil.drawableToBitmap(context.getResources().getDrawable(R.drawable.default_avatar_dark), context);
            } else {
                input = ContactUtil.drawableToBitmap(context.getResources().getDrawable(R.drawable.default_avatar), context);
            }
        }

        contactImage = Bitmap.createScaledBitmap(input, MainActivity.contactWidth, MainActivity.contactWidth, true);

        InputStream input2;

        try {
            input2 = ContactUtil.openDisplayPhoto(Long.parseLong(this.myId), context);
        } catch (NumberFormatException e) {
            input2 = null;
        }

        if (input2 == null) {
            if (darkContactImage) {
                input2 = context.getResources().openRawResource(R.drawable.default_avatar_dark);
            } else {
                input2 = context.getResources().openRawResource(R.drawable.default_avatar);
            }
        }

        Bitmap im;

        try {
            im = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input2), MainActivity.contactWidth, MainActivity.contactWidth, true);
        } catch (Exception e) {
            if (darkContactImage) {
                im = Bitmap.createScaledBitmap(ContactUtil.drawableToBitmap(context.getResources().getDrawable(R.drawable.default_avatar_dark), context), MainActivity.contactWidth, MainActivity.contactWidth, true);
            } else {
                im = Bitmap.createScaledBitmap(ContactUtil.drawableToBitmap(context.getResources().getDrawable(R.drawable.default_avatar), context), MainActivity.contactWidth, MainActivity.contactWidth, true);
            }
        }

        myImage = im;

        paint = new Paint();
        float densityMultiplier = context.getResources().getDisplayMetrics().density;
        float scaledPx = Integer.parseInt(textSize) * densityMultiplier;
        paint.setTextSize(scaledPx);
        font = null;

        if (customFont) {
            font = Typeface.createFromFile(sharedPrefs.getString("custom_font_path", ""));
            paint.setTypeface(font);
        }
    }

    private int getItemViewType(Cursor query) {
        try {
            String s = query.getString(query.getColumnIndex("msg_box"));

            if (s != null) {
                if (query.getInt(query.getColumnIndex("msg_box")) == 4) {
                    return 1;
                } else if (query.getInt(query.getColumnIndex("msg_box")) == 5) {
                    return 1;
                } else if (query.getInt(query.getColumnIndex("msg_box")) == 1) {
                    return 0;
                } else if (query.getInt(query.getColumnIndex("msg_box")) == 2) {
                    return 1;
                }
            } else {
                String type = query.getString(query.getColumnIndex("type"));

                if (type.equals("2") || type.equals("4") || type.equals("5") || type.equals("6")) {
                    return 1;
                } else {
                    return 0;
                }

            }
        } catch (Exception e) {
            return 0;
        }

        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(getCount() - 1 - position);
        return getItemViewType(cursor);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public void bindView(final View view, Context mContext, final Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.media.setVisibility(View.GONE);


        boolean sent = false;
        String image;
        String body = "";
        String date = "0";
        long id = 0;
        boolean sending = false;
        boolean error = false;
        boolean group = false;
        String sender = "";

        String dateType = "date";

        try {
            String s = cursor.getString(cursor.getColumnIndex("msg_box"));

            if (s != null) {
                id = cursor.getLong(cursor.getColumnIndex("_id"));
                body = "";

                date = Long.parseLong(cursor.getString(cursor.getColumnIndex("date"))) * 1000 + "";

                sender = MessageCursorAdapter.getFrom(Uri.parse("content://mms/" + cursor.getString(cursor.getColumnIndex("_id"))), context).trim();

                if (cursor.getInt(cursor.getColumnIndex("msg_box")) == 4) {
                    sending = true;
                    sent = true;
                } else if (cursor.getInt(cursor.getColumnIndex("msg_box")) == 5) {
                    error = true;
                    sent = true;
                } else if (cursor.getInt(cursor.getColumnIndex("msg_box")) == 1) {
                    sent = false;
                } else if (cursor.getInt(cursor.getColumnIndex("msg_box")) == 2) {
                    sent = true;
                }

                final String selectionPart = "mid=" + cursor.getString(cursor.getColumnIndex("_id"));

                if (!group) {
                    holder.media.setVisibility(View.VISIBLE);

                    final long idF = id;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(250);
                            } catch (Exception e) {

                            }

                            String body = "";
                            String image = null;
                            String video = null;
                            String audio = null;

                            Uri uri = Uri.parse("content://mms/part");
                            Cursor query = contentResolver.query(uri, null, selectionPart, null, null);

                            if (query.moveToFirst()) {
                                do {
                                    String partId = query.getString(query.getColumnIndex("_id"));
                                    String type = query.getString(query.getColumnIndex("ct"));
                                    String body2 = "";

                                    if ("text/plain".equals(type)) {
                                        String data = query.getString(query.getColumnIndex("_data"));
                                        if (data != null) {
                                            body2 = getMmsText(partId, context);
                                            body += body2;
                                        } else {
                                            body2 = query.getString(query.getColumnIndex("text"));
                                            body += body2;
                                        }
                                    }

                                    if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
                                            "image/gif".equals(type) || "image/jpg".equals(type) ||
                                            "image/png".equals(type)) {
                                        if (image == null) {
                                            image = "content://mms/part/" + partId;
                                        } else {
                                            image += " content://mms/part/" + partId;
                                        }
                                    }

                                    if ("video/mpeg".equals(type) || "video/3gpp".equals(type) || "video/mp4".equals(type)) {
                                        video = "content://mms/part/" + partId;
                                    }

                                    if (type.startsWith("audio/")) {
                                        audio = "content://mms/part/" + partId;
                                    }
                                } while (query.moveToNext());
                            }

                            query.close();

                            if (image == null && video == null && audio == null && body.equals("")) {
                                context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                    @Override
                                    public void run() {
                                        downloadableMessage(holder);
                                    }
                                });
                            } else {
                                String images[];
                                Bitmap picture;

                                try {
                                    holder.imageUri = Uri.parse(image);
                                    images = image.trim().split(" ");
                                    picture = BitmapFactory.decodeFile(IOUtil.getPath(Uri.parse(images[0]), context));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    images = null;
                                    picture = null;
                                } catch (Error e) {
                                    try {
                                        holder.imageUri = Uri.parse(image);
                                        images = image.trim().split(" ");
                                        picture = SendUtil.getThumbnail(context, Uri.parse(images[0]));
                                    } catch (Exception f) {
                                        images = null;
                                        picture = null;
                                    }
                                }

                                final String text = body;
                                final String imageUri = image;
                                final String[] imagesF = images;
                                final Bitmap pictureF = picture;
                                final String videoF = video;
                                final String audioF = audio;

                                if (holder.text.getText().toString().equals("")) {
                                    // view is empty and has not been recycled, so show the images
                                    context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                        @Override
                                        public void run() {
                                            setMessageText(holder.text, text);

                                            if (imageUri == null && videoF == null && audioF == null) {
                                                holder.media.setVisibility(View.GONE);
                                                holder.media.setImageResource(android.R.color.transparent);
                                            } else if (imageUri != null) {
                                                holder.media.setVisibility(View.VISIBLE);

                                                if (pictureF == null) {
                                                    holder.media.setImageURI(Uri.parse(imagesF[0]));
                                                } else {
                                                    holder.media.setImageBitmap(pictureF);
                                                }

                                                if (imagesF.length > 1) {
                                                    holder.date.setText(holder.date.getText().toString() + " - Multiple Attachments");
                                                }
                                            } else if (videoF != null) {
                                                holder.media.setVisibility(View.VISIBLE);
                                                holder.media.setImageResource(R.drawable.ic_video_play);
                                                holder.media.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                            } else if (audioF != null) {
                                                holder.media.setVisibility(View.VISIBLE);
                                                holder.media.setImageResource(R.drawable.ic_video_play);
                                                holder.media.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }).start();
                } else {
                    body = "";
                    image = null;
                    String video = null;
                    String audio = null;

                    Uri uri = Uri.parse("content://mms/part");
                    Cursor query = contentResolver.query(uri, null, selectionPart, null, null);

                    if (query.moveToFirst()) {
                        do {
                            String partId = query.getString(query.getColumnIndex("_id"));
                            String type = query.getString(query.getColumnIndex("ct"));
                            String body2 = "";

                            if ("text/plain".equals(type)) {
                                String data = query.getString(query.getColumnIndex("_data"));
                                if (data != null) {
                                    body2 = getMmsText(partId, context);
                                    body += body2;
                                } else {
                                    body2 = query.getString(query.getColumnIndex("text"));
                                    body += body2;
                                }
                            }

                            if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
                                    "image/gif".equals(type) || "image/jpg".equals(type) ||
                                    "image/png".equals(type)) {
                                if (image == null) {
                                    image = "content://mms/part/" + partId;
                                } else {
                                    image += " content://mms/part/" + partId;
                                }
                            }

                            if ("video/mpeg".equals(type) || "video/3gpp".equals(type) || "video/mp4".equals(type)) {
                                video = "content://mms/part/" + partId;
                            }

                            if (type.startsWith("audio/")) {
                                audio = "content://mms/part/" + partId;
                            }
                        } while (query.moveToNext());
                    }

                    query.close();

                    if (image == null && video == null && audio == null && body.equals("")) {
                        downloadableMessage(holder);
                    } else {
                        String images[];

                        try {
                            holder.imageUri = Uri.parse(image);
                            images = image.trim().split(" ");
                        } catch (Exception e) {
                            images = null;
                        }

                        final String text = body;
                        final String imageUri = images[0];
                        final String[] imagesF = images;
                        final String videoF = video;
                        final String audioF = audio;

                        setMessageText(holder.text, text);

                        if (imageUri == null && videoF == null && audioF == null) {
                            holder.media.setVisibility(View.GONE);
                            holder.media.setImageResource(android.R.color.transparent);
                        } else if (imageUri != null) {
                            holder.media.setVisibility(View.VISIBLE);
                            holder.media.setImageURI(Uri.parse(imageUri));
                            holder.media.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (imagesF.length == 1) {
                                        Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        intent.putExtra("SingleItemOnly", true);
                                        intent.setDataAndType(Uri.parse(imageUri), "image/*");
                                        context.startActivity(intent);
                                    } else {
                                        Intent intent = new Intent();
                                        intent.setClass(context, ImageViewer.class);
                                        Bundle b = new Bundle();
                                        b.putString("image", imageUri);
                                        intent.putExtra("bundle", b);
                                        context.startActivity(intent);
                                    }
                                }
                            });

                            if (imagesF.length > 1) {
                                holder.date.setText(holder.date.getText().toString() + " - Multiple Attachments");
                            }
                        } else if (videoF != null) {
                            holder.media.setVisibility(View.VISIBLE);
                            holder.media.setImageResource(R.drawable.ic_video_play);
                            holder.media.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            holder.media.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    intent.setDataAndType(Uri.parse(videoF), "video/*");
                                    context.startActivity(intent);
                                }
                            });
                        } else if (audioF != null) {
                            holder.media.setVisibility(View.VISIBLE);
                            holder.media.setImageResource(R.drawable.ic_video_play);
                            holder.media.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            holder.media.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.parse(audioF), "audio/*");
                                    context.startActivity(intent);
                                }
                            });
                        }
                    }
                }

            } else {
                String type = cursor.getString(cursor.getColumnIndex("type"));

                if (type.equals("1")) {
                    sent = false;

                    try {
                        body = cursor.getString(cursor.getColumnIndex("body"));
                    } catch (Exception e) {
                        body = "";
                    }

                    date = cursor.getString(cursor.getColumnIndex(dateType));
                    id = cursor.getLong(cursor.getColumnIndex("_id"));
                } else if (type.equals("2")) {
                    sent = true;
                    body = cursor.getString(cursor.getColumnIndex("body"));
                    date = cursor.getString(cursor.getColumnIndex("date"));
                    id = cursor.getLong(cursor.getColumnIndex("_id"));
                } else if (type.equals("5")) {
                    sent = true;
                    body = cursor.getString(cursor.getColumnIndex("body"));
                    date = cursor.getString(cursor.getColumnIndex("date"));
                    id = cursor.getLong(cursor.getColumnIndex("_id"));
                    error = true;
                } else if (type.equals("4") || type.equals("6")) {
                    sent = true;
                    body = cursor.getString(cursor.getColumnIndex("body"));
                    date = cursor.getString(cursor.getColumnIndex("date"));
                    id = cursor.getLong(cursor.getColumnIndex("_id"));
                    sending = true;
                } else {
                    sent = false;
                    body = cursor.getString(cursor.getColumnIndex("body"));
                    date = cursor.getString(cursor.getColumnIndex(dateType));
                    id = cursor.getLong(cursor.getColumnIndex("_id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (checkedAll) {
            holder.background.setBackgroundColor(context.getResources().getColor(R.color.holo_blue));
            holder.bubble.setColorFilter(context.getResources().getColor(R.color.holo_blue));
        } else {
            if (sent) {
                holder.background.setBackgroundColor(ctSentMessageBackground);
                holder.bubble.setColorFilter(ctSentMessageBackground);
            } else {
                holder.background.setBackgroundColor(ctRecievedMessageBackground);
                holder.bubble.setColorFilter(ctRecievedMessageBackground);
            }
        }

        if (group && !sent) {
            final String sentFrom = sender;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    final Bitmap picture = ContactUtil.getFacebookPhoto(sentFrom, context);

                    context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                        @Override
                        public void run() {
                            holder.image.setImageBitmap(picture);
                            holder.image.assignContactFromPhone(sentFrom, true);
                        }
                    });
                }

            }).start();
        }

        Date date2;

        try {
            date2 = new Date(Long.parseLong(date));
        } catch (Exception e) {
            date2 = new Date(0);
        }

        Calendar cal = Calendar.getInstance();
        Date currentDate = new Date(cal.getTimeInMillis());

        if (getZeroTimeDate(date2).equals(getZeroTimeDate(currentDate))) {
            if (hourFormat) {
                holder.date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2));
            } else {
                holder.date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2));
            }
        } else {
            if (hourFormat) {
                holder.date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2));
            } else {
                holder.date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2));
            }
        }

        if (sending == true) {
            holder.date.setVisibility(View.GONE);

            try {
                holder.ellipsis.setVisibility(View.VISIBLE);
                holder.ellipsis.setBackgroundResource(R.drawable.ellipsis);
                holder.ellipsis.setColorFilter(ctRecievedTextColor);
                AnimationDrawable ellipsis = (AnimationDrawable) holder.ellipsis.getBackground();
                ellipsis.start();
            } catch (Exception e) {

            }
        } else {
            holder.date.setVisibility(View.VISIBLE);

            try {
                holder.ellipsis.setVisibility(View.GONE);
            } catch (Exception e) {

            }

            if (error) {
                String text = "<html><body><img src=\"ic_error.png\"/> ERROR</body></html>";
                holder.date.setText(Html.fromHtml(text, imgGetterFail, null));
            }
        }

        if (group == true && sent == false) {
            final String senderF = sender;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(senderF.replaceAll("-", "")));
                    final Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[]{ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts._ID}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc limit 1");

                    context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                        @Override
                        public void run() {
                            if (phonesCursor != null && phonesCursor.moveToFirst()) {
                                holder.date.setText(holder.date.getText() + " - " + phonesCursor.getString(0));
                            } else {
                                holder.date.setText(holder.date.getText() + " - " + senderF);
                            }

                            phonesCursor.close();
                        }

                    });
                }
            }).start();
        }

        holder.text.setText(body);

        if (cursor.getPosition() == 0) {
            if (runAs.equals("hangout")) {
                int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, context.getResources().getDisplayMetrics());
                int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());
                int scale3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
                int scale4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 21, context.getResources().getDisplayMetrics());

                if (sent) {
                    view.setPadding(scale4, scale2, scale, scale3);
                } else {
                    view.setPadding(scale, scale2, scale4, scale3);
                }
            } else if (runAs.equals("card2")) {
                int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, context.getResources().getDisplayMetrics());
                int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
                int scale3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());
                view.setPadding(scale, scale2, scale, scale3);
            } else {
                int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());
                view.setPadding(0, 0, 0, scale);
            }
        } else {
            if (runAs.equals("hangout")) {
                int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, context.getResources().getDisplayMetrics());
                int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());
                int scale3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 21, context.getResources().getDisplayMetrics());

                if (sent) {
                    view.setPadding(scale3, scale2, scale, scale2);
                } else {
                    view.setPadding(scale, scale2, scale3, scale2);
                }
            } else if (runAs.equals("card2")) {
                int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, context.getResources().getDisplayMetrics());
                int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
                view.setPadding(scale, scale2, scale, 0);
            } else {
                view.setPadding(0, 0, 0, 0);
            }
        }

        final long idF = id;
        final boolean sentF = sent;

        holder.background.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                boolean flag = false;
                int pos = 0;

                for (int i = 0; i < itemsToDelete.size(); i++) {
                    if (itemsToDelete.get(i).equals(idF)) {
                        flag = true;
                        pos = i;
                        break;
                    }
                }

                if (!flag) {
                    itemsToDelete.add(idF);
                    holder.background.setBackgroundColor(context.getResources().getColor(R.color.holo_blue));
                    holder.bubble.setColorFilter(context.getResources().getColor(R.color.holo_blue));
                } else {
                    itemsToDelete.remove(pos);

                    if (sentF) {
                        holder.background.setBackgroundColor(ctSentMessageBackground);
                        holder.bubble.setColorFilter(ctSentMessageBackground);
                    } else {
                        holder.background.setBackgroundColor(ctRecievedMessageBackground);
                        holder.bubble.setColorFilter(ctRecievedMessageBackground);
                    }
                }

            }

        });

        if (itemsToDelete.contains(id)) {
            holder.background.setBackgroundColor(context.getResources().getColor(R.color.holo_blue));
            holder.bubble.setColorFilter(context.getResources().getColor(R.color.holo_blue));
        } else {
            if (sentF) {
                holder.background.setBackgroundColor(ctSentMessageBackground);
                holder.bubble.setColorFilter(ctSentMessageBackground);
            } else {
                holder.background.setBackgroundColor(ctRecievedMessageBackground);
                holder.bubble.setColorFilter(ctRecievedMessageBackground);
            }
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        View v;

        int type = getItemViewType(cursor);

        if (type == 1) {
            if (runAs.equals("hangout")) {
                v = mInflater.inflate(R.layout.message_hangout_sent, parent, false);
            } else if (runAs.equals("sliding")) {
                v = mInflater.inflate(R.layout.message_classic_sent, parent, false);
            } else {
                v = mInflater.inflate(R.layout.message_card2_sent, parent, false);
            }

            holder.text = (TextView) v.findViewById(R.id.textBody);
            holder.date = (TextView) v.findViewById(R.id.textDate);
            holder.media = (ImageView) v.findViewById(R.id.media);
            holder.image = (QuickContactBadge) v.findViewById(R.id.imageContactPicture);
            try {
                holder.ellipsis = (ImageView) v.findViewById(R.id.ellipsis);
            } catch (Exception e) {
            }
            holder.bubble = (ImageView) v.findViewById(R.id.msgBubble);
            holder.background = v.findViewById(R.id.messageBody);

            holder.image.assignContactUri(ContactsContract.Profile.CONTENT_URI);
        } else {
            if (runAs.equals("hangout")) {
                v = mInflater.inflate(R.layout.message_hangout_received, parent, false);
            } else if (runAs.equals("sliding")) {
                v = mInflater.inflate(R.layout.message_classic_received, parent, false);
            } else {
                v = mInflater.inflate(R.layout.message_card2_received, parent, false);
            }

            holder.text = (TextView) v.findViewById(R.id.textBody);
            holder.date = (TextView) v.findViewById(R.id.textDate);
            holder.media = (ImageView) v.findViewById(R.id.media);
            holder.image = (QuickContactBadge) v.findViewById(R.id.imageContactPicture);
            holder.downloadButton = (Button) v.findViewById(R.id.downloadButton);
            holder.bubble = (ImageView) v.findViewById(R.id.msgBubble);
            holder.background = v.findViewById(R.id.messageBody);

            holder.image.assignContactFromPhone(inboxNumbers, true);
        }

        if (runAs.equals("card2")) {

            if (themeName.equals("Light Theme") || themeName.equals("Hangouts Theme") || themeName.equals("Light Theme 2.0") || themeName.equals("Light Green Theme") || themeName.equals("Burnt Orange Theme")) {

            } else {
                v.findViewById(R.id.shadow).setVisibility(View.GONE);
            }

            if (type == 1) {
                v.findViewById(R.id.divider).setBackgroundColor(convertToColorInt(convertToARGB(ctSentTextColor, "44")));
            } else {
                v.findViewById(R.id.divider).setBackgroundColor(convertToColorInt(convertToARGB(ctRecievedTextColor, "44")));
            }
        }

        if (customFont) {
            holder.text.setTypeface(font);
            holder.date.setTypeface(font);
        }

        if (contactPictures) {
            if (type == 0) {
                holder.image.setImageBitmap(contactImage);
            } else {
                holder.image.setImageBitmap(myImage);
            }
        } else {
            holder.image.setMaxWidth(0);
            holder.image.setMinimumWidth(0);
        }

        try {
            holder.text.setTextSize(Integer.parseInt(textSize.substring(0, 2)));
            holder.date.setTextSize(Integer.parseInt(textSize.substring(0, 2)) - 4);
        } catch (Exception e) {
            holder.text.setTextSize(Integer.parseInt(textSize.substring(0, 1)));
            holder.date.setTextSize(Integer.parseInt(textSize.substring(0, 1)) - 4);
        }

        if (tinyDate) {
            holder.date.setTextSize(10);
        }

        holder.text.setText("");
        holder.date.setText("");

        if (type == 0) {
            holder.downloadButton.setVisibility(View.GONE);
        }

        if (type == 1) {
            holder.text.setTextColor(ctSentTextColor);
            holder.date.setTextColor(convertToColorInt(convertToARGB(ctSentTextColor, "55")));
            holder.background.setBackgroundColor(ctSentMessageBackground);
            holder.media.setBackgroundColor(ctSentMessageBackground);
            holder.bubble.setColorFilter(ctSentMessageBackground);

            if (!customTheme) {
                if (sentTextColor.equals("blue")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.holo_blue));
                    holder.date.setTextColor(context.getResources().getColor(R.color.holo_blue));
                } else if (sentTextColor.equals("white")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.white));
                    holder.date.setTextColor(context.getResources().getColor(R.color.white));
                } else if (sentTextColor.equals("green")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.holo_green));
                    holder.date.setTextColor(context.getResources().getColor(R.color.holo_green));
                } else if (sentTextColor.equals("orange")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.holo_orange));
                    holder.date.setTextColor(context.getResources().getColor(R.color.holo_orange));
                } else if (sentTextColor.equals("red")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.holo_red));
                    holder.date.setTextColor(context.getResources().getColor(R.color.holo_red));
                } else if (sentTextColor.equals("purple")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.holo_purple));
                    holder.date.setTextColor(context.getResources().getColor(R.color.holo_purple));
                } else if (sentTextColor.equals("black")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.pitch_black));
                    holder.date.setTextColor(context.getResources().getColor(R.color.pitch_black));
                } else if (sentTextColor.equals("grey")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.grey));
                    holder.date.setTextColor(context.getResources().getColor(R.color.grey));
                }

                holder.background.setBackgroundColor(convertToColorInt(convertToARGB(ctSentMessageBackground, textOpacity + "")));
            }
        } else {
            holder.text.setTextColor(ctRecievedTextColor);
            holder.date.setTextColor(convertToColorInt(convertToARGB(ctRecievedTextColor, "55")));
            holder.background.setBackgroundColor(ctRecievedMessageBackground);
            holder.media.setBackgroundColor(ctRecievedMessageBackground);
            holder.bubble.setColorFilter(ctRecievedMessageBackground);

            if (!customTheme) {
                if (receivedTextColor.equals("blue")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.holo_blue));
                    holder.date.setTextColor(context.getResources().getColor(R.color.holo_blue));
                } else if (receivedTextColor.equals("white")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.white));
                    holder.date.setTextColor(context.getResources().getColor(R.color.white));
                } else if (receivedTextColor.equals("green")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.holo_green));
                    holder.date.setTextColor(context.getResources().getColor(R.color.holo_green));
                } else if (receivedTextColor.equals("orange")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.holo_orange));
                    holder.date.setTextColor(context.getResources().getColor(R.color.holo_orange));
                } else if (receivedTextColor.equals("red")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.holo_red));
                    holder.date.setTextColor(context.getResources().getColor(R.color.holo_red));
                } else if (receivedTextColor.equals("purple")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.holo_purple));
                    holder.date.setTextColor(context.getResources().getColor(R.color.holo_purple));
                } else if (receivedTextColor.equals("black")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.pitch_black));
                    holder.date.setTextColor(context.getResources().getColor(R.color.pitch_black));
                } else if (receivedTextColor.equals("grey")) {
                    holder.text.setTextColor(context.getResources().getColor(R.color.grey));
                    holder.date.setTextColor(context.getResources().getColor(R.color.grey));
                }

                holder.background.setBackgroundColor(convertToColorInt(convertToARGB(ctRecievedMessageBackground, textOpacity + "")));
            }
        }

        if (!textAlignment.equals("split")) {
            if (textAlignment.equals("right")) {
                holder.text.setGravity(Gravity.RIGHT);
                holder.date.setGravity(Gravity.RIGHT);
            } else {
                holder.text.setGravity(Gravity.LEFT);
                holder.date.setGravity(Gravity.LEFT);
            }
        } else if (!contactPictures) {
            if (type == 0) {
                holder.text.setGravity(Gravity.LEFT);
                holder.date.setGravity(Gravity.LEFT);
            } else {
                holder.text.setGravity(Gravity.RIGHT);
                holder.date.setGravity(Gravity.RIGHT);
            }
        }

        if (runAs.equals("hangout")) {
            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, context.getResources().getDisplayMetrics());
            int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());
            int scale3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 21, context.getResources().getDisplayMetrics());

            if (type == 1) {
                v.setPadding(scale3, scale2, scale, scale2);
            } else {
                v.setPadding(scale, scale2, scale3, scale2);
            }
        } else if (runAs.equals("card2")) {
            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, context.getResources().getDisplayMetrics());
            int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
            v.setPadding(scale, scale2, scale, 0);
        }

        v.setTag(holder);
        return v;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (!mCursor.moveToPosition(getCount() - 1 - position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        View v;
        if (convertView == null) {
            v = newView(context, mCursor, parent);
        } else {
            v = convertView;
        }
        bindView(v, context, mCursor);
        return v;
    }

    static class ViewHolder {
        public TextView text;
        public TextView date;
        public QuickContactBadge image;
        public View background;
        public ImageView media;
        public ImageView ellipsis;
        public Button downloadButton;
        public ImageView bubble;
        public Uri imageUri;
    }

    public void setMessageText(final TextView textView, final String body) {
        textView.setText(body);
    }

    public void downloadableMessage(final ViewHolder holder) {
        holder.text.setText("Undownloaded MMS message");
    }

    private static String getMmsText(String id, Activity context) {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = context.getContentResolver().openInputStream(partURI);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return sb.toString();
    }

    public static Date getZeroTimeDate(Date fecha) {
        Date res;
        Calendar cal = Calendar.getInstance();

        cal.setTime(fecha);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        res = cal.getTime();

        return res;
    }

    Html.ImageGetter imgGetterFail = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            Drawable drawable = context.getResources().getDrawable(R.drawable.ic_failed);

            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
                    .getIntrinsicHeight());

            return drawable;
        }
    };

    public static String convertToARGB(int color, String a) {
        String alpha = a;

        if (alpha.length() > 2) {
            alpha = "FF";
        }

        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }

    public static int convertToColorInt(String argb) throws NumberFormatException {

        if (argb.startsWith("#")) {
            argb = argb.replace("#", "");
        }

        int alpha = -1, red = -1, green = -1, blue = -1;

        if (argb.length() == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        } else if (argb.length() == 6) {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }

        return Color.argb(alpha, red, green, blue);
    }
}