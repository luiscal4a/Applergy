package com.example.applergy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applergy.rest.ExtendedIngredient;
import com.example.applergy.rest.Recipe;
import com.example.applergy.rest.Step;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder>{

    private ArrayList<Recipe> list_recipes;

    public RecipeAdapter(ArrayList<Recipe> list_recipes) {
        this.list_recipes = list_recipes;
    }

    @NonNull
    @Override
    public RecipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeAdapter.ViewHolder holder, int position) {
        holder.tv_name.setText(list_recipes.get(position).getTitle());

        if(!list_recipes.get(holder.getAdapterPosition()).getVegetarian())
            holder.imv_vegetarian.setVisibility(View.GONE);
        if(!list_recipes.get(holder.getAdapterPosition()).getGlutenFree())
            holder.imv_gluten.setVisibility(View.GONE);
        if(!list_recipes.get(holder.getAdapterPosition()).getDairyFree())
            holder.imv_dairy.setVisibility(View.GONE);

        String url =  list_recipes.get(holder.getAdapterPosition()).getImage();
        if(URLUtil.isValidUrl(url))
            Picasso.get()
                    .load(url)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            // Image loaded successfully
                            holder.imv_cover.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            // Image failed to load
                            holder.imv_cover.setImageResource(R.drawable.applergy_2);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            // Image is being prepared to load
                            holder.imv_cover.setImageResource(R.drawable.applergy_2);
                        }
                    });
        else
            Picasso.get()
                    .load(R.drawable.applergy_2)
                    .into(holder.imv_cover);

        holder.btn_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                ViewGroup viewGroup = holder.btn_detail.findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_recipe, viewGroup, false);

                ImageView imv_cover = dialogView.findViewById(R.id.imv_cover);
                TextView tv_title = dialogView.findViewById(R.id.tv_title);
                ImageView imv_dairy = dialogView.findViewById(R.id.imv_dairy);
                ImageView imv_gluten = dialogView.findViewById(R.id.imv_gluten);
                ImageView imv_vegetarian = dialogView.findViewById(R.id.imv_vegetarian);
                TextView tv_desc = dialogView.findViewById(R.id.tv_desc);
                TextView tv_ingredients = dialogView.findViewById(R.id.tv_ingredients);
                TextView tv_steps = dialogView.findViewById(R.id.tv_steps);
                TextView tv_preparation = dialogView.findViewById(R.id.tv_preparation);
                TextView tv_servings = dialogView.findViewById(R.id.tv_servings);

                tv_title.setText(list_recipes.get(holder.getAdapterPosition()).getTitle());

                String description = list_recipes.get(holder.getAdapterPosition()).getSummary();

                int lastIndex = description.lastIndexOf(". ");
                description = description.substring(0, lastIndex + 1);

                tv_desc.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY));
                tv_preparation.setText("Preparation: "+ list_recipes.get(holder.getAdapterPosition()).getReadyInMinutes() + " minutes");
                tv_servings.setText("Servings: "+ list_recipes.get(holder.getAdapterPosition()).getServings());

                if(!list_recipes.get(holder.getAdapterPosition()).getVegetarian())
                    imv_vegetarian.setVisibility(View.GONE);
                if(!list_recipes.get(holder.getAdapterPosition()).getGlutenFree())
                    imv_gluten.setVisibility(View.GONE);
                if(!list_recipes.get(holder.getAdapterPosition()).getDairyFree())
                    imv_dairy.setVisibility(View.GONE);

                if(URLUtil.isValidUrl(url))
                    Picasso.get()
                            .load(url)
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    // Image loaded successfully
                                    imv_cover.setImageBitmap(bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                    // Image failed to load
                                    imv_cover.setImageResource(R.drawable.applergy_2);
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                    // Image is being prepared to load
                                    imv_cover.setImageResource(R.drawable.applergy_2);
                                }
                            });
                else
                    Picasso.get()
                            .load(R.drawable.applergy_2)
                            .into(imv_cover);

                List<ExtendedIngredient> ingredients = list_recipes.get(holder.getAdapterPosition()).getExtendedIngredients();
                String str_ingredients = "";

                for(ExtendedIngredient ingredient : ingredients) {
                    str_ingredients += "- " + ingredient.getOriginal() + "\n";
                }

                tv_ingredients.setText(str_ingredients);


                List<Step> steps = list_recipes.get(holder.getAdapterPosition()).getAnalyzedInstructions().get(0).getSteps();
                String str_steps = "";

                for(Step step : steps) {
                    str_steps += step.getNumber() + ". " + step.getStep() + "\n";
                }

                tv_steps.setText(str_steps);

                builder.setView(dialogView)
                        .setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                Toast.makeText(view.getContext(),"Item saved",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                /*Toast.makeText(view.getContext(),"you choose no action for alertbox",
                                        Toast.LENGTH_SHORT).show();*/
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return list_recipes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_name;
        private ImageButton btn_detail;

        private ImageView imv_dairy, imv_gluten, imv_vegetarian, imv_cover;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_name = itemView.findViewById(R.id.tv_name);
            btn_detail = itemView.findViewById(R.id.btn_detail);
            imv_dairy = itemView.findViewById(R.id.imv_dairy);
            imv_gluten = itemView.findViewById(R.id.imv_gluten);
            imv_vegetarian = itemView.findViewById(R.id.imv_vegetarian);
            imv_cover = itemView.findViewById(R.id.imv_cover);
        }
    }
}
