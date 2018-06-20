package com.wordplat.quickstart.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wordplat.ikvstockchart.KLineHandler;
import com.wordplat.ikvstockchart.drawing.BOLLDrawing;
import com.wordplat.ikvstockchart.drawing.KLineVolumeDrawing;
import com.wordplat.ikvstockchart.drawing.KLineVolumeHighlightDrawing;
import com.wordplat.ikvstockchart.entry.Entry;
import com.wordplat.ikvstockchart.entry.SizeColor;
import com.wordplat.ikvstockchart.entry.StockBOLLIndex;
import com.wordplat.ikvstockchart.entry.StockKLineVolumeIndex;
import com.wordplat.ikvstockchart.entry.StockPlaceIndex;
import com.wordplat.ikvstockchart.render.TimeLineRender;
import com.wordplat.quickstart.R;
import com.wordplat.quickstart.utils.AppUtils;
import com.wordplat.ikvstockchart.InteractiveKLineView;
import com.wordplat.ikvstockchart.compat.PerformenceAnalyser;
import com.wordplat.ikvstockchart.drawing.HighlightDrawing;
import com.wordplat.ikvstockchart.drawing.KDJDrawing;
import com.wordplat.ikvstockchart.drawing.MACDDrawing;
import com.wordplat.ikvstockchart.drawing.RSIDrawing;
import com.wordplat.ikvstockchart.drawing.StockIndexYLabelDrawing;
import com.wordplat.ikvstockchart.entry.EntrySet;
import com.wordplat.ikvstockchart.entry.StockDataTest;
import com.wordplat.ikvstockchart.entry.StockKDJIndex;
import com.wordplat.ikvstockchart.entry.StockMACDIndex;
import com.wordplat.ikvstockchart.entry.StockRSIIndex;
import com.wordplat.ikvstockchart.marker.XAxisTextMarkerView;
import com.wordplat.ikvstockchart.marker.YAxisTextMarkerView;
import com.wordplat.ikvstockchart.render.KLineRender;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.InputStream;

/**
 * <p>MACD_RSI_KDJ_Show_Together_Activity</p>
 * <p>Date: 2017/3/31</p>
 *
 * @author afon
 */

@ContentView(R.layout.activity_macd_rsi_kdj_show_together)
public class MACD_RSI_KDJ_Show_Together_Activity extends BaseActivity implements View.OnClickListener{

    @ViewInject(R.id.kLineView) private InteractiveKLineView kLineView = null;
    @ViewInject(R.id.MA_Text) private TextView MA_Text = null;
    @ViewInject(R.id.StockIndex_Text) private TextView StockIndex_Text = null;
    @ViewInject(R.id.Volume_Text) private TextView Volume_Text = null;
    @ViewInject(R.id.tv_vol)private TextView tv_vol;
    @ViewInject(R.id.But_Group)private RadioGroup But_Group;
    @ViewInject(R.id.MACD_But)private RadioButton MACD_But;
    @ViewInject(R.id.RSI_But)private RadioButton RSI_But;
    @ViewInject(R.id.KDJ_But)private RadioButton KDJ_But;
    @ViewInject(R.id.BOLL_But)private RadioButton BOLL_But;
    @ViewInject(R.id.MA_But)private RadioButton MA_But;
    @ViewInject(R.id.btn_fen)private Button btn_fen;
    @ViewInject(R.id.btn_k)private Button btn_k;

    private KLineRender kLineRender;
    private TimeLineRender timeLineRender;

    private StockMACDIndex macdIndex;
    private StockRSIIndex rsiIndex;
    private StockKDJIndex kdjIndex;
    private StockBOLLIndex bollIndex;
    private String currDraw = "ma";

    private final EntrySet entrySet = new EntrySet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MACD_But.setOnClickListener(this);
        RSI_But.setOnClickListener(this);
        KDJ_But.setOnClickListener(this);
        BOLL_But.setOnClickListener(this);
        MA_But.setOnClickListener(this);
        kLineView.setEnableLeftRefresh(false);
        kLineView.setEnableRightRefresh(false);
        btn_fen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initTimeUi();
                kLineView.notifyDataSetChanged();
            }
        });
        btn_k.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initUI();
                kLineView.notifyDataSetChanged();
            }
        });
        initTimeUi();
        loadKLineData();
    }

    private void initTimeUi()
    {
//        if(timeLineRender != null)
//        {
//            kLineView.setRender(timeLineRender);
//            kLineView.notifyDataSetChanged();
//            return;
//        }
        timeLineRender = new TimeLineRender(this);
        final int stockMarkerViewHeight = AppUtils.dpTopx(mActivity, 15);
        timeLineRender.setDataHeight(stockMarkerViewHeight);

        // 成交量
        StockKLineVolumeIndex kLineVolumeIndex = new StockKLineVolumeIndex();
        kLineVolumeIndex.addDrawing(new KLineVolumeDrawing());
        kLineVolumeIndex.addDrawing(new KLineVolumeHighlightDrawing());
        timeLineRender.addStockIndex(kLineVolumeIndex);

        // MACD
        HighlightDrawing macdHighlightDrawing = new HighlightDrawing();
        macdHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        macdIndex = new StockMACDIndex();
        macdIndex.addDrawing(new MACDDrawing());
        macdIndex.addDrawing(new StockIndexYLabelDrawing());
        macdIndex.addDrawing(macdHighlightDrawing);
//        macdIndex.setPaddingTop(paddingTop);
        timeLineRender.addStockIndex(macdIndex);


        // RSI
        HighlightDrawing rsiHighlightDrawing = new HighlightDrawing();
        rsiHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        rsiIndex = new StockRSIIndex();
        rsiIndex.addDrawing(new RSIDrawing());
        rsiIndex.addDrawing(new StockIndexYLabelDrawing());
        rsiIndex.addDrawing(rsiHighlightDrawing);
//        rsiIndex.setPaddingTop(paddingTop);
        timeLineRender.addStockIndex(rsiIndex);

        // KDJ
        HighlightDrawing kdjHighlightDrawing = new HighlightDrawing();
        kdjHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        kdjIndex = new StockKDJIndex();
        kdjIndex.addDrawing(new KDJDrawing());
        kdjIndex.addDrawing(new StockIndexYLabelDrawing());
        kdjIndex.addDrawing(kdjHighlightDrawing);
//        kdjIndex.setPaddingTop(paddingTop);
        timeLineRender.addStockIndex(kdjIndex);

        // BOLL
        HighlightDrawing bollHighlightDrawing = new HighlightDrawing();
        bollHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        bollIndex = new StockBOLLIndex();
        bollIndex.addDrawing(new BOLLDrawing());
        bollIndex.addDrawing(new StockIndexYLabelDrawing());
        bollIndex.addDrawing(bollHighlightDrawing);
//        bollIndex.setPaddingTop(paddingTop);
        timeLineRender.addStockIndex(bollIndex);

        StockPlaceIndex stockPlaceIndex = new StockPlaceIndex(stockMarkerViewHeight);
        timeLineRender.addStockIndex(stockPlaceIndex);

        timeLineRender.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));
        timeLineRender.addMarkerView(new XAxisTextMarkerView(stockMarkerViewHeight));
        initKlineHandler();
        macdIndex.setEnable(false);
        rsiIndex.setEnable(false);
        kdjIndex.setEnable(false);
        bollIndex.setEnable(false);
        kLineView.setRender(timeLineRender);
    }

    private void initUI() {
//        if(kLineRender != null) {
//            kLineView.setRender(kLineRender);
//            kLineView.notifyDataSetChanged();
//            return;
//        }
        kLineRender = new KLineRender(this);
        //        final int paddingTop = AppUtils.dpTopx(mActivity, 10);
        final int stockMarkerViewHeight = AppUtils.dpTopx(mActivity, 15);
        kLineRender.setDataHeight(stockMarkerViewHeight);

        // 成交量
        StockKLineVolumeIndex kLineVolumeIndex = new StockKLineVolumeIndex();
        kLineVolumeIndex.addDrawing(new KLineVolumeDrawing());
        kLineVolumeIndex.addDrawing(new KLineVolumeHighlightDrawing());
        kLineRender.addStockIndex(kLineVolumeIndex);

        // MACD
        HighlightDrawing macdHighlightDrawing = new HighlightDrawing();
        macdHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        macdIndex = new StockMACDIndex();
        macdIndex.addDrawing(new MACDDrawing());
        macdIndex.addDrawing(new StockIndexYLabelDrawing());
        macdIndex.addDrawing(macdHighlightDrawing);
//        macdIndex.setPaddingTop(paddingTop);
        kLineRender.addStockIndex(macdIndex);


        // RSI
        HighlightDrawing rsiHighlightDrawing = new HighlightDrawing();
        rsiHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        rsiIndex = new StockRSIIndex();
        rsiIndex.addDrawing(new RSIDrawing());
        rsiIndex.addDrawing(new StockIndexYLabelDrawing());
        rsiIndex.addDrawing(rsiHighlightDrawing);
//        rsiIndex.setPaddingTop(paddingTop);
        kLineRender.addStockIndex(rsiIndex);

        // KDJ
        HighlightDrawing kdjHighlightDrawing = new HighlightDrawing();
        kdjHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        kdjIndex = new StockKDJIndex();
        kdjIndex.addDrawing(new KDJDrawing());
        kdjIndex.addDrawing(new StockIndexYLabelDrawing());
        kdjIndex.addDrawing(kdjHighlightDrawing);
//        kdjIndex.setPaddingTop(paddingTop);
        kLineRender.addStockIndex(kdjIndex);

        // BOLL
        HighlightDrawing bollHighlightDrawing = new HighlightDrawing();
        bollHighlightDrawing.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));

        bollIndex = new StockBOLLIndex();
        bollIndex.addDrawing(new BOLLDrawing());
        bollIndex.addDrawing(new StockIndexYLabelDrawing());
        bollIndex.addDrawing(bollHighlightDrawing);
//        bollIndex.setPaddingTop(paddingTop);
        kLineRender.addStockIndex(bollIndex);

        StockPlaceIndex stockPlaceIndex = new StockPlaceIndex(stockMarkerViewHeight);
        kLineRender.addStockIndex(stockPlaceIndex);

        kLineRender.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));
        kLineRender.addMarkerView(new XAxisTextMarkerView(stockMarkerViewHeight));
        initKlineHandler();
        macdIndex.setEnable(false);
        rsiIndex.setEnable(false);
        kdjIndex.setEnable(false);
        bollIndex.setEnable(false);
        kLineView.setRender(kLineRender);
    }


    private void initKlineHandler()
    {
        kLineView.setKLineHandler(new KLineHandler() {
            @Override
            public void onLeftRefresh() {

            }

            @Override
            public void onRightRefresh() {

            }

            @Override
            public void onSingleTap(MotionEvent e, float x, float y) {

            }

            @Override
            public void onDoubleTap(MotionEvent e, float x, float y) {

            }

            @Override
            public void onHighlight(Entry entry, int entryIndex, float x, float y) {
                SizeColor sizeColor;
                if(kLineView.getRender() instanceof KLineRender)
                    sizeColor  = kLineRender.getSizeColor();
                else
                    sizeColor  = timeLineRender.getSizeColor();

                immediatelyShow(entry,sizeColor,currDraw);

                String volumeString = String.format(getResources().getString(R.string.volume_highlight),
                        entry.getVolumeMa5(),
                        entry.getVolumeMa10());

                Volume_Text.setText(getSpannableString(volumeString,
                        sizeColor.getMa5Color(),
                        sizeColor.getMa10Color(),
                        0));

                SpannableString spanString = new SpannableString("");
                if (isShownMACD()) {
                    String str = String.format(getResources().getString(R.string.macd_highlight),
                            entry.getDiff(),
                            entry.getDea(),
                            entry.getMacd());

                    spanString = getSpannableString(str,
                            sizeColor.getDiffLineColor(),
                            sizeColor.getDeaLineColor(),
                            sizeColor.getMacdHighlightTextColor());

                } else if (isShownKDJ()) {
                    String str = String.format(getResources().getString(R.string.kdj_highlight),
                            entry.getK(),
                            entry.getD(),
                            entry.getJ());

                    spanString = getSpannableString(str,
                            sizeColor.getKdjKLineColor(),
                            sizeColor.getKdjDLineColor(),
                            sizeColor.getKdjJLineColor());

                } else if (isShownRSI()) {
                    String str = String.format(getResources().getString(R.string.rsi_highlight),
                            entry.getRsi1(),
                            entry.getRsi2(),
                            entry.getRsi3());

                    spanString = getSpannableString(str,
                            sizeColor.getRsi1LineColor(),
                            sizeColor.getRsi2LineColor(),
                            sizeColor.getRsi3LineColor());

                } else if (isShownBOLL()) {
                    String str = String.format(getResources().getString(R.string.boll_highlight),
                            entry.getMb(),
                            entry.getUp(),
                            entry.getDn());

                    spanString = getSpannableString(str,
                            sizeColor.getBollMidLineColor(),
                            sizeColor.getBollUpperLineColor(),
                            sizeColor.getBollLowerLineColor());
                }
                StockIndex_Text.setText(spanString);
            }

            @Override
            public void onCancelHighlight() {

            }
        });
    }
    private  void immediatelyShow(Entry entry,SizeColor sizeColor,String draw)
    {
        if(TextUtils.equals(draw,"ma"))
        {
            String maString = String.format(getResources().getString(R.string.ma_highlight),
                    entry.getMa5(),
                    entry.getMa10(),
                    entry.getMa20());

            MA_Text.setText(getSpannableString(maString,
                    sizeColor.getMa5Color(),
                    sizeColor.getMa10Color(),
                    sizeColor.getMa20Color()));
        }
        else if(TextUtils.equals(draw,"boll"))
        {
            String maString = String.format(getResources().getString(R.string.boll_highlight),
                    entry.getMb(),
                    entry.getUp(),
                    entry.getDn());

            MA_Text.setText(getSpannableString(maString,
                    sizeColor.getBollMidLineColor(),
                    sizeColor.getBollUpperLineColor(),
                    sizeColor.getBollLowerLineColor()));
        }
    }

    public boolean isShownMACD() {
        return macdIndex.isEnable();
    }

    public boolean isShownRSI() {
        return rsiIndex.isEnable();
    }

    public boolean isShownKDJ() {
        return kdjIndex.isEnable();
    }

    public boolean isShownBOLL() {
        return bollIndex.isEnable();
    }

    private SpannableString getSpannableString(String str, int partColor0, int partColor1, int partColor2) {
        String[] splitString = str.split("[●]");
        SpannableString spanString = new SpannableString(str);

        int pos0 = splitString[0].length();
        int pos1 = pos0 + splitString[1].length() + 1;
        int end = str.length();

        spanString.setSpan(new ForegroundColorSpan(partColor0),
                pos0, pos1, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);

        if (splitString.length > 2) {
            int pos2 = pos1 + splitString[2].length() + 1;

            spanString.setSpan(new ForegroundColorSpan(partColor1),
                    pos1, pos2, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);

            spanString.setSpan(new ForegroundColorSpan(partColor2),
                    pos2, end, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);
        } else {
            spanString.setSpan(new ForegroundColorSpan(partColor1),
                    pos1, end, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);
        }

        return spanString;
    }


    public void showMACD() {
        macdIndex.setEnable(true);
        rsiIndex.setEnable(false);
        kdjIndex.setEnable(false);
        bollIndex.setEnable(false);
    }

    public void showRSI() {
        macdIndex.setEnable(false);
        rsiIndex.setEnable(true);
        kdjIndex.setEnable(false);
        bollIndex.setEnable(false);
    }

    public void showKDJ() {
        macdIndex.setEnable(false);
        rsiIndex.setEnable(false);
        kdjIndex.setEnable(true);
        bollIndex.setEnable(false);
    }

    public void showBOLL() {
        macdIndex.setEnable(false);
        rsiIndex.setEnable(false);
        kdjIndex.setEnable(false);
        bollIndex.setEnable(true);
    }

    private void loadKLineData() {
        new AsyncTask<Void, Void, Void>() {

            private EntrySet entrySet;

            @Override
            protected Void doInBackground(Void... params) {

                PerformenceAnalyser.getInstance().addWatcher();

                String kLineData = "";
                try {
                    InputStream in = getResources().getAssets().open("kline1.txt");
                    int length = in.available();
                    byte[] buffer = new byte[length];
                    in.read(buffer);
                    kLineData = new String(buffer, "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                PerformenceAnalyser.getInstance().addWatcher();

                entrySet = StockDataTest.parseKLineData(kLineData);

                PerformenceAnalyser.getInstance().addWatcher();

                entrySet.computeStockIndex();

                PerformenceAnalyser.getInstance().addWatcher();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                kLineView.setEntrySet(entrySet);

                PerformenceAnalyser.getInstance().addWatcher();

                kLineView.notifyDataSetChanged();

                PerformenceAnalyser.getInstance().addWatcher();
            }
        }.execute();
    }

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, MACD_RSI_KDJ_Show_Together_Activity.class);
        return intent;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.BOLL_But:
                currDraw = "boll";
                if(kLineView.getRender() instanceof KLineRender)
                    kLineRender.replaceDraw("boll");
                else
                    timeLineRender.replaceDraw("boll");
                break;
            case R.id.MA_But:
                currDraw = "ma";
                if(kLineView.getRender() instanceof KLineRender)
                    kLineRender.replaceDraw("ma");
                else
                    timeLineRender.replaceDraw("ma");
                break;
            case R.id.MACD_But:
                if(kLineView.getRender() instanceof KLineRender) {
                    showMACD();
                    kLineRender.setLineCount(3);
                    showNum();
                }
                else {
                    showMACD();
                    timeLineRender.setLineCount(3);
                    showNum();
                }
                break;
            case R.id.RSI_But:
                if(kLineView.getRender() instanceof KLineRender) {
                    showRSI();
                    kLineRender.setLineCount(3);
                    showNum();
                }
                else {
                    showRSI();
                    timeLineRender.setLineCount(3);
                    showNum();
                }
                break;
            case R.id.KDJ_But:
                if(kLineView.getRender() instanceof KLineRender) {
                    showKDJ();
                    kLineRender.setLineCount(3);
                    showNum();
                }
                else {
                    showKDJ();
                    timeLineRender.setLineCount(3);
                    showNum();
                }
                break;
        }
        kLineView.notifyDataSetChanged();
    }

    private void showNum()
    {
        RelativeLayout.LayoutParams lp1 = (RelativeLayout.LayoutParams) tv_vol.getLayoutParams();
        lp1.bottomMargin = AppUtils.dpTopx(this,146);
        tv_vol.setLayoutParams(lp1);
        RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) Volume_Text.getLayoutParams();
        lp2.bottomMargin = AppUtils.dpTopx(this,146);
        Volume_Text.setLayoutParams(lp2);
        StockIndex_Text.setVisibility(View.VISIBLE);
    }
}
