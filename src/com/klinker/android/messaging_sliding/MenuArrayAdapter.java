package com.klinker.android.messaging_sliding;

import android.text.Spanned;
import android.widget.RelativeLayout;
import com.klinker.android.messaging_donate.R;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.klinker.android.messaging_sliding.emojis.*;
import com.klinker.android.messaging_sliding.theme.CustomTheme;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.QuickContactBadge;
import android.widget.TextView;

public class MenuArrayAdapter extends ArrayAdapter<String> {
  private final Activity context;
  private final ArrayList<String> body;
  private final ArrayList<String> date;
  private final ArrayList<String> numbers;
  private final ArrayList<String> threadIds;
  private final ArrayList<String> group;
  private final ArrayList<String> count;
  private final ArrayList<String> read;
  private final ViewPager pager;
  private static final String FILENAME = "newMessages.txt";
  private SharedPreferences sharedPrefs;
  
  static class ViewHolder {
	    public TextView text;
	    public TextView text2;
	    public TextView text3;
	    public TextView text4;
	    public QuickContactBadge image;
	  }

  public MenuArrayAdapter(Activity context, ArrayList<String> body, ArrayList<String> date, ArrayList<String> numbers, ViewPager pager, ArrayList<String> threadIds, ArrayList<String> group, ArrayList<String> count, ArrayList<String> read) {
    super(context, R.layout.contact_body, body);
    this.context = context;
    this.body = body;
    this.date = date;
    this.numbers = numbers;
    this.threadIds = threadIds;
    this.group = group;
    this.count = count;
    this.read = read;
    this.pager = pager;
    this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
  }
  
  @Override
  public int getCount()
  {
	return body.size();
  }

  @SuppressLint("SimpleDateFormat")
  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
	  View contactView = convertView;
	  
	  if (contactView == null)
	  {
		  LayoutInflater inflater = (LayoutInflater) context
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
		  contactView = inflater.inflate(R.layout.contact_body, parent, false);
		  ViewHolder viewHolder = new ViewHolder();
		  viewHolder.text = (TextView) contactView.findViewById(R.id.contactName);
		  viewHolder.text2 = (TextView) contactView.findViewById(R.id.contactBody);
		  viewHolder.text3 = (TextView) contactView.findViewById(R.id.contactDate);
		  viewHolder.text4 = (TextView) contactView.findViewById(R.id.contactDate2);
		  viewHolder.image = (QuickContactBadge) contactView.findViewById(R.id.quickContactBadge3);
		  
		  if (sharedPrefs.getBoolean("custom_font", false))
	      {
	    	  viewHolder.text.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
	    	  viewHolder.text2.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
	    	  viewHolder.text3.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
	    	  viewHolder.text4.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
	      }

          if (!sharedPrefs.getBoolean("custom_theme", false))
          {
              String color = sharedPrefs.getString("menu_text_color", "default");

              if (color.equals("blue"))
              {
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.holo_blue));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.holo_blue));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.holo_blue));
              } else if (color.equals("white"))
              {
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.white));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.white));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.white));
              } else if (color.equals("green"))
              {
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.holo_green));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.holo_green));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.holo_green));
              } else if (color.equals("orange"))
              {
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.holo_orange));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.holo_orange));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.holo_orange));
              } else if (color.equals("red"))
              {
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.holo_red));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.holo_red));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.holo_red));
              } else if (color.equals("purple"))
              {
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.holo_purple));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.holo_purple));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.holo_purple));
              } else if (color.equals("black"))
              {
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.pitch_black));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.pitch_black));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.pitch_black));
              } else if (color.equals("grey"))
              {
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.grey));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.grey));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.grey));
              }  else
              {
                  viewHolder.text2.setTextColor(sharedPrefs.getInt("ct_summaryTextColor", context.getResources().getColor(R.color.black)));
                  viewHolder.text3.setTextColor(sharedPrefs.getInt("ct_summaryTextColor", context.getResources().getColor(R.color.black)));
                  viewHolder.text4.setTextColor(sharedPrefs.getInt("ct_summaryTextColor", context.getResources().getColor(R.color.black)));
              }
          } else
          {
              viewHolder.text.setTextColor(sharedPrefs.getInt("ct_nameTextColor", context.getResources().getColor(R.color.black)));
              viewHolder.text2.setTextColor(sharedPrefs.getInt("ct_summaryTextColor", context.getResources().getColor(R.color.black)));
              viewHolder.text3.setTextColor(sharedPrefs.getInt("ct_summaryTextColor", context.getResources().getColor(R.color.black)));
              viewHolder.text4.setTextColor(sharedPrefs.getInt("ct_summaryTextColor", context.getResources().getColor(R.color.black)));
          }

          viewHolder.text.setTextSize((float)Integer.parseInt(sharedPrefs.getString("text_size2", 14 + "")));
          viewHolder.text2.setTextSize((float)Integer.parseInt(sharedPrefs.getString("text_size2", 14 + "")));
          viewHolder.text3.setTextSize((float)(Integer.parseInt(sharedPrefs.getString("text_size2", 14 + "")) - 2));
          viewHolder.text4.setTextSize((float)(Integer.parseInt(sharedPrefs.getString("text_size2", 14 + "")) - 2));

          if (!sharedPrefs.getBoolean("contact_pictures2", true))
          {
              viewHolder.image.setVisibility(View.GONE);
              RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) viewHolder.text.getLayoutParams();
              RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) viewHolder.text2.getLayoutParams();
              params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
              params2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
              viewHolder.text.setLayoutParams(params1);
              viewHolder.text2.setLayoutParams(params2);
          }
		  
		  contactView.setTag(viewHolder);
	  }
	  
	  final ViewHolder holder = (ViewHolder) contactView.getTag();

      final String number = MainActivity.findContactNumber(numbers.get(position), context);

      holder.image.assignContactFromPhone(number, true);
      holder.image.setMode(ContactsContract.QuickContact.MODE_LARGE);

	  new Thread(new Runnable() {

		@Override
		public void run() {
			final Bitmap image = getFacebookPhoto(number);

            Spanned text;
            String names = "";

            if (!sharedPrefs.getBoolean("hide_message_counter", false))
            {
                if (group.get(position).equals("yes"))
                {
                    if (Integer.parseInt(count.get(position)) > 1)
                    {
                        text = Html.fromHtml("Group MMS   <font color=#" + CustomTheme.convertToARGB(sharedPrefs.getInt("ct_messageCounterColor", context.getResources().getColor(R.color.messageCounterLight))).substring(3) + "><b>" + count.get(position) + "</b></color>");
                    } else
                    {
                        text = Html.fromHtml("Group MMS");
                    }

                    names = MainActivity.loadGroupContacts(number, context);
                } else
                {
                    String contactName = MainActivity.findContactName(number, context);

                    if (Integer.parseInt(count.get(position)) > 1)
                    {
                        text = Html.fromHtml(contactName + "   <font color=#" + CustomTheme.convertToARGB(sharedPrefs.getInt("ct_messageCounterColor", context.getResources().getColor(R.color.messageCounterLight))).substring(3) + "><b>" + count.get(position) + "</b></color>");
                    } else
                    {
                        text = Html.fromHtml(contactName);
                    }
                }
            } else
            {
                if (group.get(position).equals("yes"))
                {
                    text = Html.fromHtml("Group MMS");
                    names = MainActivity.loadGroupContacts(number, context);
                } else
                {
                    text = Html.fromHtml(MainActivity.findContactName(number, context));
                }
            }

            final Spanned textF = text;
            final String namesF = names;

		  	context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

				@Override
				public void run() {
					if (sharedPrefs.getBoolean("contact_pictures2", true))
					{
                        if (group.get(position).equals("no"))
                        {
                            try
                              {
                                holder.image.setImageBitmap(Bitmap.createScaledBitmap(image, MainActivity.contactWidth, MainActivity.contactWidth, true));
                              } catch (Exception e)
                              {
                                if (sharedPrefs.getBoolean("ct_darkContactImage", false))
                                {
                                    holder.image.setImageBitmap(Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_dark)), MainActivity.contactWidth, MainActivity.contactWidth, true));
                                } else
                                {
                                    holder.image.setImageBitmap(Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_picture)), MainActivity.contactWidth, MainActivity.contactWidth, true));
                                }
                              }
                        } else
                        {
                            if (sharedPrefs.getBoolean("ct_darkContactImage", false))
                            {
                                holder.image.setImageBitmap(Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_dark)), MainActivity.contactWidth, MainActivity.contactWidth, true));
                            } else
                            {
                                holder.image.setImageBitmap(Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_picture)), MainActivity.contactWidth, MainActivity.contactWidth, true));
                            }
                        }
					} else
					{
						holder.text2.setPadding(10, 0, 0, 15);
					}

                    holder.text.setText(textF);

                    if (group.get(position).equals("yes"))
                    {
                        holder.text2.setText(namesF);
                    }
				}

		    });
		}

	  }).start();

	  if (sharedPrefs.getString("smilies", "with").equals("with"))
	  {
		  String patternStr = "[^\\x20-\\x7E]";
		  Pattern pattern = Pattern.compile(patternStr);
		  Matcher matcher = pattern.matcher(body.get(position));

		  if (matcher.find())
		  {
			  if (sharedPrefs.getBoolean("emoji_type", true))
			  {
				  holder.text2.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter2.getSmiledText(context, body.get(position))));
			  } else
			  {
				  holder.text2.setText(EmojiConverter.getSmiledText(context, EmoticonConverter2.getSmiledText(context, body.get(position))));
			  }
	      } else
		  {
			  holder.text2.setText(EmoticonConverter2.getSmiledText(context, body.get(position)));
		  }
	  } else if (sharedPrefs.getString("smilies", "with").equals("without"))
	  {
		  String patternStr = "[^\\x20-\\x7E]";
		  Pattern pattern = Pattern.compile(patternStr);
		  Matcher matcher = pattern.matcher(body.get(position));

		  if (matcher.find())
		  {
			  if (sharedPrefs.getBoolean("emoji_type", true))
			  {
				  holder.text2.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter2.getSmiledText(context, body.get(position))));
			  } else
			  {
				  holder.text2.setText(EmojiConverter.getSmiledText(context, EmoticonConverter2.getSmiledText(context, body.get(position))));
			  }
		  } else
		  {
			  holder.text2.setText(EmoticonConverter.getSmiledText(context, body.get(position)));
		  }
	  } else if (sharedPrefs.getString("smilies", "with").equals("none"))
	  {
		  String patternStr = "[^\\x20-\\x7E]";
		  Pattern pattern = Pattern.compile(patternStr);
		  Matcher matcher = pattern.matcher(body.get(position));

		  if (matcher.find())
		  {
			  if (sharedPrefs.getBoolean("emoji_type", true))
			  {
				  holder.text2.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter2.getSmiledText(context, body.get(position))));
			  } else
			  {
				  holder.text2.setText(EmojiConverter.getSmiledText(context, EmoticonConverter2.getSmiledText(context, body.get(position))));
			  }
		  } else
		  {
			  holder.text2.setText(body.get(position));
		  }
	  } else if (sharedPrefs.getString("smilies", "with").equals("both"))
	  {
		  String patternStr = "[^\\x20-\\x7E]";
		  Pattern pattern = Pattern.compile(patternStr);
		  Matcher matcher = pattern.matcher(body.get(position));

		  if (matcher.find())
		  {
			  if (sharedPrefs.getBoolean("emoji_type", true))
			  {
				  holder.text2.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter2.getSmiledText(context, body.get(position))));
			  } else
			  {
				  holder.text2.setText(EmojiConverter.getSmiledText(context, EmoticonConverter2.getSmiledText(context, body.get(position))));
			  }
	      } else
		  {
			  holder.text2.setText(EmoticonConverter3.getSmiledText(context, body.get(position)));
		  }
	  }

	  Date date2 = new Date(0);

	  try
	  {
		  date2 = new Date(Long.parseLong(date.get(position)));
	  } catch (Exception e)
	  {

	  }

	  if (sharedPrefs.getBoolean("hour_format", false))
	  {
		  holder.text3.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2));
	  } else
	  {
		  holder.text3.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2));
	  }

	  holder.text4.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(date2));

      if (MainActivity.deviceType.equals("phablet") || MainActivity.deviceType.equals("tablet"))
      {
          holder.text3.setText("");
          holder.text4.setText("");
          holder.text.setMaxLines(1);
          holder.text2.setMaxLines(1);
      }

	  if (read.get(position).equals("0"))
	  {
		  if (position != 0)
		  {
			  if (!sharedPrefs.getBoolean("custom_background", false))
		        {
				  contactView.setBackgroundColor(sharedPrefs.getInt("ct_unreadConversationColor", sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white))));
		        }

			  read.set(position, "1");
		  } else
		  {
			  if (MainActivity.sentMessage != true)
			  {
				  if (!sharedPrefs.getBoolean("custom_background", false))
			        {
					  contactView.setBackgroundColor(sharedPrefs.getInt("ct_unreadConversationColor", sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white))));
			        }

				  read.set(position, "1");
			  }
		  }
	  }

	  final View contactView2 = contactView;

	  contactView.setOnClickListener(new View.OnClickListener()
		{
		    public void onClick(View v) {
                if (MainActivity.deviceType.equals("phone") || MainActivity.deviceType.equals("phablet2"))
                {
                    MainActivity.waitToLoad = true;
                }

		    	pager.setCurrentItem(position, false);
			    read.set(position, "1");
			    MainActivity.menu.showContent();

		        if (!sharedPrefs.getBoolean("custom_background", false))
		        {
		        	contactView2.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", context.getResources().getColor(R.color.light_silver)));
		        }

		        new Thread(new Runnable() {

					@Override
					public void run() {
						try
						{
							ContentValues values = new ContentValues();
			               	values.put("read", true);
			               	context.getContentResolver().update(Uri.parse("content://sms/conversations/"), values, "thread_id=?", new String[] {threadIds.get(position)});
			               	context.getContentResolver().update(Uri.parse("content://mms/conversations/"), values, "thread_id=?", new String[] {threadIds.get(position)});
						} catch (Exception e)
						{
							e.printStackTrace();
						}

						ArrayList<String> newMessages = readFromFile(context);

				        for (int j = 0; j < newMessages.size(); j++)
				        {
				        	if (newMessages.get(j).replace("+", "").replace("+1", "").replace("-", "").equals(holder.text.getText().toString().replace("+", "").replace("+1", "").replace("-", "")))
				        	{
				        		newMessages.remove(j);
				        	}
				        }

				        writeToFile(newMessages, context);

					}

		        }).start();
		    }
		});

	  contactView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {

				Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		        vibrator.vibrate(25);

				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage(context.getResources().getString(R.string.delete_messages) + "\n\n" + context.getResources().getString(R.string.conversation) + ": " + MainActivity.findContactName(number, context));
				builder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
			               final ProgressDialog progDialog = new ProgressDialog(context);
			               progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			               progDialog.setMessage(context.getResources().getString(R.string.deleting));
			               progDialog.show();

			               new Thread(new Runnable(){

								@Override
								public void run() {
									deleteSMS(context, threadIds.get(position));

									context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

										@Override
										public void run() {
											((MainActivity)context).refreshViewPager(true);
											progDialog.dismiss();
										}

								    });
								}

			               }).start();
			           }


				public void deleteSMS(Context context, String id) {
				    try {
				        context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/"), "thread_id=?", new String[]{id});
				    } catch (Exception e) {
				    }
				}});
				builder.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               dialog.dismiss();
			           }
			       });
				AlertDialog dialog = builder.create();

				dialog.show();
				return false;
			}

		});

      if (!sharedPrefs.getBoolean("custom_theme", false))
      {
          String color = sharedPrefs.getString("name_text_color", "default");

          if (color.equals("blue"))
          {
              holder.text.setText(holder.text.getText().toString());
              holder.text.setTextColor(context.getResources().getColor(R.color.holo_blue));
          } else if (color.equals("white"))
          {
              holder.text.setText(holder.text.getText().toString());
              holder.text.setTextColor(context.getResources().getColor(R.color.white));
          } else if (color.equals("green"))
          {
              holder.text.setText(holder.text.getText().toString());
              holder.text.setTextColor(context.getResources().getColor(R.color.holo_green));
          } else if (color.equals("orange"))
          {
              holder.text.setText(holder.text.getText().toString());
              holder.text.setTextColor(context.getResources().getColor(R.color.holo_orange));
          } else if (color.equals("red"))
          {
              holder.text.setText(holder.text.getText().toString());
              holder.text.setTextColor(context.getResources().getColor(R.color.holo_red));
          } else if (color.equals("purple"))
          {
              holder.text.setText(holder.text.getText().toString());
              holder.text.setTextColor(context.getResources().getColor(R.color.holo_purple));
          } else if (color.equals("black"))
          {
              holder.text.setText(holder.text.getText().toString());
              holder.text.setTextColor(context.getResources().getColor(R.color.pitch_black));
          } else if (color.equals("grey"))
          {
              holder.text.setText(holder.text.getText().toString());
              holder.text.setTextColor(context.getResources().getColor(R.color.grey));
          }  else
          {
              holder.text.setTextColor(sharedPrefs.getInt("ct_nameTextColor", context.getResources().getColor(R.color.black)));
          }
      }

	  return contactView;
  }
  
  public InputStream openDisplayPhoto(long contactId) {
	  Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
	     Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
	     Cursor cursor = context.getContentResolver().query(photoUri,
	          new String[] {Contacts.Photo.PHOTO}, null, null, null);
	     if (cursor == null) {
	         return null;
	     }
	     try {
	         if (cursor.moveToFirst()) {
	             byte[] data = cursor.getBlob(0);
	             if (data != null) {
	                 return new ByteArrayInputStream(data);
	             }
	         }
	     } finally {
	         cursor.close();
	     }
	     return null;
	 }
  
  	private ArrayList<String> readFromFile(Context context) {
		
      ArrayList<String> ret = new ArrayList<String>();
      
      try {
          InputStream inputStream = context.openFileInput(FILENAME);
          
          if ( inputStream != null ) {
          	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
          	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
          	String receiveString = "";
          	
          	while ( (receiveString = bufferedReader.readLine()) != null ) {
          		ret.add(receiveString);
          	}
          	
          	inputStream.close();
          }
      }
      catch (FileNotFoundException e) {
      	
		} catch (IOException e) {
			
		}

      return ret;
	}
  	
  	public Bitmap getFacebookPhoto(String phoneNumber) {
        try
        {
            Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Uri photoUri = null;
            ContentResolver cr = context.getContentResolver();
            Cursor contact = cr.query(phoneUri,
                    new String[] { ContactsContract.Contacts._ID }, null, null, null);

            try
            {
                if (contact.moveToFirst()) {
                    long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
                    photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);
                    contact.close();
                }
                else {
                    Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);

                    if (sharedPrefs.getBoolean("ct_darkContactImage", false))
                    {
                        defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
                    }

                    contact.close();
                    return defaultPhoto;
                }
                if (photoUri != null) {
                    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                            cr, photoUri);
                    if (input != null) {
                        contact.close();
                        return BitmapFactory.decodeStream(input);
                    }
                } else {
                    Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);

                    if (sharedPrefs.getBoolean("ct_darkContactImage", false))
                    {
                        defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
                    }

                    contact.close();
                    return defaultPhoto;
                }
                Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);

                if (sharedPrefs.getBoolean("ct_darkContactImage", false))
                {
                    defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
                }

                contact.close();
                return defaultPhoto;
            } catch (Exception e)
            {
                if (sharedPrefs.getBoolean("ct_darkContactImage", false))
                {
                    contact.close();
                    return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
                } else
                {
                    contact.close();
                    return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
                }
            }
        } catch (Exception e)
        {
            if (sharedPrefs.getBoolean("ct_darkContactImage", false))
            {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
            } else
            {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
            }
        }
	}
  
  public Bitmap drawableToBitmap (Drawable drawable) {
	    if (drawable instanceof BitmapDrawable) {
	        return ((BitmapDrawable)drawable).getBitmap();
	    }

	    try
	    {
		    int width = drawable.getIntrinsicWidth();
		    width = width > 0 ? width : 1;
		    int height = drawable.getIntrinsicHeight();
		    height = height > 0 ? height : 1;
	
		    Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		    Canvas canvas = new Canvas(bitmap); 
		    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		    drawable.draw(canvas);
		    return bitmap;
	    } catch (Exception e)
	    {
	    	return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
	    }
	}
  	
  	private void writeToFile(ArrayList<String> data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(FILENAME, Context.MODE_PRIVATE));
            
            for (int i = 0; i < data.size(); i++)
            {
            	outputStreamWriter.write(data.get(i) + "\n");
            }
            	
            outputStreamWriter.close();
        }
        catch (IOException e) {
            
        } 
		
	}
} 