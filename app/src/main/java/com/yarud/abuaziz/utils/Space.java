package com.yarud.abuaziz.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class Space extends RecyclerView.ItemDecoration {
    private int space;
    public Space(int space){
        this.space = space;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (!(parent.getChildLayoutPosition(view) == 0 )){
            if (parent.getChildLayoutPosition(view) == 1){
                outRect.top = space;
            }
            outRect.right = space;
            outRect.left = space;
            outRect.bottom = space;
        }
    }
}
