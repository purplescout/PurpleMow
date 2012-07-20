
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <unistd.h>

#include <gtk/gtk.h>

#include <libintl.h>
#include <libgen.h>

#include "mower_net.h"
#include "mower_calc.h"

#include "sensor_obstacle.h"
#include "sensor_bwf.h"
#include "sensor_voltage.h"
#include "sensor_moisture.h"
#include "sensor_tables.h"
#include "position.h"

#include "purplesim.h"

#define PURPLESIM_SAVE_FILE         "purplesim.world"

#define SAVE_STRING_SENSOR_RANGE    "sensor range: "
#define SAVE_STRING_SENSOR_BWF      "sensor bwf: "
#define SAVE_STRING_SENSOR_VOLTAGE  "sensor voltage: "
#define SAVE_STRING_SENSOR_MOISTURE "sensor moisture: "
#define SAVE_STRING_VOLTAGE         "voltage: "
#define SAVE_STRING_MOISTURE        "moisture: "
#define SAVE_STRING_PIXELSCALE      "pixelscale: "
#define SAVE_STRING_MAXSPEED        "maxspeed: "
#define SAVE_STRING_OBSTACLE        "obstacle: "
#define SAVE_STRING_BWF             "bwf: "

#define BUTTON_COMMANDS             "Commands"
#define BUTTON_READ_VALUES              "Read Values"
#define BUTTON_RIGHT_MOTOR_INVERTED      "Right Motor Inverted"

#define DEFAULT_LAWN_SIZE_X 600
#define DEFAULT_LAWN_SIZE_Y 400

#define MOWER_SIDE      10.0
#define MOWER_SIDE_INT  10

#define _(x) gettext (x)
#define N_(x) (x)

#define GETTEXT_PACKAGE "purplesim"

#define min(x,y)    (x) < (y) ? (x) : (y)

//#define BWF_PRINT
//#define MOWER_PRINT
//#define SAVE_PRINT
//#define SENSOR_PRINT

enum list_type {
    TYPE_NONE,
    TYPE_BWF,
    TYPE_OBSTACLE,
};

struct list_item {
    enum list_type      type;
    GtkTreeIter         iter;
    GdkRectangle*       item;
};

struct mower {
    struct position     current_pos;
    struct position     new_pos;
    struct mower_pos    mow_pos;
};

struct options {
    char                port[6];
};

struct purplesim {
    GtkWidget*          window;
    GtkWidget*          drawing;
    GtkWidget*          label;
    GtkWidget*          treeview1;
    GtkListStore*       liststore;
    GtkWidget*          cbox_sensor_range;
    GtkWidget*          cbox_sensor_bwf;
    GtkWidget*          cbox_sensor_voltage;
    GtkWidget*          cbox_sensor_moisture;
    GtkWidget*          cbox_pixelscale;
    GtkWidget*          cbox_maxspeed;
    GtkWidget*          cbox_voltage;
    GtkWidget*          cbox_moisture;
    GList*              bwf_list;
    GList*              obstacle_list;
    struct list_item    marked_item;
    struct mower        mowers[MAX_MOWERS];
    int                 bwf_sensor;
    int                 range_sensor;
    int                 voltage_sensor;
    int                 moisture_sensor;
    int                 pixelscale;
    int                 maxspeed;
    int                 voltage;
    int                 moisture;
    int                 debug_commands;
    int                 debug_values;
    int                 right_motor_inverted;
};

enum {
    COLUMN_NAME,
    COLUMN_X,
    COLUMN_Y,
    COLUMN_WIDTH,
    COLUMN_HEIGHT,
    N_COLUMNS
};

static void draw_rectangle( GtkWidget *widget, GdkGC* gc, GdkRectangle* rectangle);
static void draw_point( GtkWidget *widget, GdkGC* gc, GdkPoint* point);
static void draw_mower(GtkWidget *widget, int nbr);

struct purplesim this;

int purplesim_get_sensor_value(enum purplesim_sensor sensor, int mower)
{
    int value = 0;
    GList* current;

    struct position pos;

    pos = this.mowers[mower].current_pos;

#ifdef SENSOR_PRINT
    printf("old pos: %f %f\n", pos.x, pos.y);
#endif // SENSOR_PRINT

    switch ( sensor ) {
        case purplesim_sensor_range_left:
        case purplesim_sensor_bwf_left:
            pos.x += cos(-this.mowers[mower].mow_pos.direction + pi / 4.0) * MOWER_SIDE / 2.0;
            pos.y -= sin(-this.mowers[mower].mow_pos.direction + pi / 4.0) * MOWER_SIDE / 2.0;
            break;
        case purplesim_sensor_range_right:
        case purplesim_sensor_bwf_right:
            pos.x += cos(-this.mowers[mower].mow_pos.direction - pi / 4.0) * MOWER_SIDE / 2.0;
            pos.y -= sin(-this.mowers[mower].mow_pos.direction - pi / 4.0) * MOWER_SIDE / 2.0;
            break;
        default:
            break;
    }
#ifdef SENSOR_PRINT
    printf("new pos: %f %f %f\n", pos.x, pos.y, this.mowers[mower].mow_pos.direction);
#endif // SENSOR_PRINT

    if ( mower >= MAX_MOWERS || mower < 0 )
        return 0;

    gdk_threads_enter();

    switch ( sensor ) {
        case purplesim_sensor_range_left:
        case purplesim_sensor_range_right:
            current = g_list_first(this.obstacle_list);
            if ( current != NULL ) {
                double distance;
                struct obstacle obs;
                obs.x = ((GdkRectangle*)(current->data))->x;
                obs.y = ((GdkRectangle*)(current->data))->y;
                obs.width = ((GdkRectangle*)(current->data))->width;
                obs.height = ((GdkRectangle*)(current->data))->height;
                distance = sensor_obstacle_get_distance( &obs, &pos );
                current = g_list_next(current);
                while ( current != NULL ) {
                    double distance2;
                    obs.x = ((GdkRectangle*)(current->data))->x;
                    obs.y = ((GdkRectangle*)(current->data))->y;
                    obs.width = ((GdkRectangle*)(current->data))->width;
                    obs.height = ((GdkRectangle*)(current->data))->height;
                    distance2 = sensor_obstacle_get_distance( &obs, &pos );
                    current = g_list_next(current);
                    distance = min(distance, distance2);
                }
                value = sensor_obstacle_get_value(distance, this.range_sensor);
            }
            break;

        case purplesim_sensor_bwf_left:
        case purplesim_sensor_bwf_right:
            current = g_list_first(this.bwf_list);
            if ( current != NULL ) {
                double distance;
                struct bwf b;
                b.x = ((GdkRectangle*)(current->data))->x;
                b.y = ((GdkRectangle*)(current->data))->y;
                b.width = ((GdkRectangle*)(current->data))->width;
                b.height = ((GdkRectangle*)(current->data))->height;
                distance = sensor_bwf_get_distance( &b, &pos );
                current = g_list_next(current);
                while ( current != NULL ) {
                    double distance2;
                    b.x = ((GdkRectangle*)(current->data))->x;
                    b.y = ((GdkRectangle*)(current->data))->y;
                    b.width = ((GdkRectangle*)(current->data))->width;
                    b.height = ((GdkRectangle*)(current->data))->height;
                    distance2 = sensor_bwf_get_distance( &b, &pos );
                    current = g_list_next(current);
                    distance = min(distance, distance2);
                }
                value = sensor_bwf_get_value(distance, this.bwf_sensor);
            }
            break;

        case purplesim_sensor_voltage:
            value = sensor_voltage_get_value(this.voltage, this.voltage_sensor);
            break;

        case purplesim_sensor_moisture:
            value = sensor_moisture_get_value(this.moisture, this.moisture_sensor);
            break;

        default:
            break;
    }

    gdk_threads_leave();

    return value;
}

static gboolean cb_delete(GtkWidget *window, gpointer data)
{
  gtk_main_quit();
  return FALSE;
}

static GdkPixmap *pixmap = NULL;

static void redraw_pixmap(GtkWidget *widget)
{
    GList* current;
    GdkGC* gc;

    if (pixmap)
        gdk_pixmap_unref(pixmap);

    pixmap = gdk_pixmap_new(widget->window,
                            widget->allocation.width,
                            widget->allocation.height,
                            -1);

    gdk_draw_rectangle (pixmap,
                        widget->style->fg_gc[GTK_STATE_NORMAL],
                        TRUE,
                        0, 0,
                        widget->allocation.width,
                        widget->allocation.height);

    gc = widget->style->black_gc;

    current = g_list_first(this.bwf_list);
    while ( current != NULL ) {
        draw_rectangle(widget, gc, current->data);
        current = g_list_next(current);
    }

    current = g_list_first(this.obstacle_list);
    while ( current != NULL ) {
        draw_rectangle(widget, gc, current->data);
        current = g_list_next(current);
    }
}

static gint configure_event(GtkWidget *widget, GdkEventConfigure *event)
{
    redraw_pixmap(widget);

    return TRUE;
}

static gint compare_rectangles( gconstpointer aa, gconstpointer bb)
{
    GdkRectangle* a = (GdkRectangle*)aa;
    GdkRectangle* b = (GdkRectangle*)bb;

    if ( a->x == b->x &&
         a->y == b->y &&
         a->width == b->width &&
         a->height == b->height )
    {
        return 0;
    }

    return 1;
}

static GdkRectangle* find_rectangle(GList* list, gint x, gint y, gint width, gint height)
{
    GdkRectangle rectangle;
    rectangle.x = x;
    rectangle.y = y;
    rectangle.width = width;
    rectangle.height = height;

    list = g_list_find_custom(list,
                              &rectangle,
                              compare_rectangles);

    return list->data;
}

static GdkRectangle* find_bwf(gint x, gint y, gint width, gint height)
{
    return find_rectangle(this.bwf_list, x, y, width, height);
}

static GdkRectangle* find_obstacle(gint x, gint y, gint width, gint height)
{
    return find_rectangle(this.obstacle_list, x, y, width, height);
}

static GList* remove_rectangle(GList* list, GdkRectangle* rectangle)
{
    GList* item;

    item = g_list_find_custom(list,
                              rectangle,
                              compare_rectangles);

    if ( item != NULL ) {
        list = g_list_remove(list, item->data);
    }

    return list;
}

static void remove_bwf(GdkRectangle* bwf)
{
    this.bwf_list = remove_rectangle(this.bwf_list, bwf);
}

static void remove_obstacle(GdkRectangle* obstacle)
{
    this.obstacle_list = remove_rectangle(this.obstacle_list, obstacle);
}

static void draw_rectangle( GtkWidget *widget, GdkGC* gc, GdkRectangle* rectangle)
{
    gdk_draw_rectangle (pixmap,
                        gc,
                        TRUE,
                        rectangle->x, rectangle->y,
                        rectangle->width, rectangle->height);

    gtk_widget_draw(widget, rectangle);
}

static void draw_point( GtkWidget *widget, GdkGC* gc, GdkPoint* point)
{
    GdkRectangle rectangle;
    gdk_draw_point (pixmap,
                    gc,
                    point->x, point->y);

    rectangle.x = point->x;
    rectangle.y = point->y;
    rectangle.width = 1;
    rectangle.height = 1;

    gtk_widget_draw(widget, &rectangle);
}

static void add_bwf ( GtkWidget *widget, GdkGC* gc, gdouble x, gdouble y, gint width, gint height)
{
    static GdkRectangle bwf_new = { .x = 0, .y = 0, .width = 10, .height = 10 };
    GdkRectangle bwf_old;
    GdkRectangle* bwf;
    GtkTreeIter iter;
    gint width_offset = 0;
    gint height_offset = 0;

    bwf = malloc(sizeof(*bwf));

#ifdef BWF_PRINT
    printf("bwf: %f %f\n", x, y);
#endif // BWF_PRINT

    bwf_old = bwf_new;

    if ( width > -1 && height > -1 ) {
        bwf_new.x = x;
        bwf_new.y = y;
        bwf_new.width = width;
        bwf_new.height = height;
        bwf->x = bwf_new.x;
        bwf->y = bwf_new.y;
        bwf->width = bwf_new.width;
        bwf->height = bwf_new.height;
    } else {
        bwf_new.x = x;
        bwf_new.y = y;
        bwf_new.width = 10;
        bwf_new.height = 10;

#ifdef BWF_PRINT
        printf("new: %d %d removing %d %d\n", 
                bwf_new.x,
                bwf_new.y,
                bwf_new.x % 10,
                bwf_new.y % 10);
#endif // BWF_PRINT
        bwf_new.x -= bwf_new.x % 10;
        bwf_new.y -= bwf_new.y % 10;

        if ( bwf_new.x > bwf_old.x )
        {
            bwf->width = bwf_new.x - bwf_old.x;
            bwf->x = bwf_old.x;
        }
        else
        {
            bwf->width = bwf_old.x - bwf_new.x;
            width_offset = 10;
            bwf->x = bwf_new.x;
        }

        if ( bwf_new.y > bwf_old.y )
        {
            bwf->height = bwf_new.y - bwf_old.y;
            bwf->y = bwf_old.y;
        }
        else
        {
            bwf->height = bwf_old.y - bwf_new.y;
            height_offset = 10;
            bwf->y = bwf_new.y;
        }

        if ( bwf->width > bwf->height )
        {
#ifdef BWF_PRINT
            printf("width > height: %d > %d\n",
                    bwf->width,
                    bwf->height );
#endif // BWF_PRINT
            bwf->height = 10;
            bwf->width += width_offset;
            bwf->y = bwf_old.y;
            bwf_new.y = bwf_old.y;
        }
        else
        {
#ifdef BWF_PRINT
            printf("width < height: %d < %d\n",
                    bwf->width,
                    bwf->height );
#endif // BWF_PRINT
            bwf->width = 10;
            bwf->height += height_offset;
            bwf->x = bwf_old.x;
            bwf_new.x = bwf_old.x;
        }
    }


#ifdef BWF_PRINT
    printf("bwf:\n  new: %d %d %d %d\n  old: %d %d %d %d\n  sum: %d %d %d %d\n",
            bwf_new.x,
            bwf_new.y,
            bwf_new.width,
            bwf_new.height,
            bwf_old.x,
            bwf_old.y,
            bwf_old.width,
            bwf_old.height,
            bwf->x,
            bwf->y,
            bwf->width,
            bwf->height );
#endif // BWF_PRINT

    gtk_list_store_append(this.liststore, &iter);
    gtk_list_store_set(this.liststore, &iter,
                       COLUMN_NAME, "bwf",
                       COLUMN_X, bwf->x,
                       COLUMN_Y, bwf->y,
                       COLUMN_WIDTH, bwf->width,
                       COLUMN_HEIGHT, bwf->height,
                       -1);
    this.bwf_list = g_list_append(this.bwf_list, bwf);

    draw_rectangle( widget, gc, bwf);
}

void initialize_mower(int mower)
{
    if ( mower > MAX_MOWERS || mower < 0 )
        return;

    gdk_threads_enter();

    this.mowers[mower].current_pos.x = 22.5;
    this.mowers[mower].current_pos.y = 22.5;
    this.mowers[mower].new_pos.x = this.mowers[mower].current_pos.x;
    this.mowers[mower].new_pos.y = this.mowers[mower].current_pos.y;
    init_mower_pos(&this.mowers[mower].mow_pos, 50.0);

    gdk_threads_leave();
}

void move_mower(int left, int right, int mower)
{
    if ( mower > MAX_MOWERS || mower < 0 )
        return;

    gdk_threads_enter();

    // left and right are inverted, because calculations are done with mathematical cordinates
    // and drawing is done with computer graphics cordinates
    update_positions( &this.mowers[mower].mow_pos,
                      ( right / 255.0 ) * (double)this.maxspeed,
                      ( left / 255.0 ) * (double)this.maxspeed);

#ifdef MOWER_PRINT
    printf("m1: %f %f\n", this.mowers[mower].mow_pos.left.x, this.mowers[mower].mow_pos.left.y );
    printf("m2: %f %f\n", this.mowers[mower].mow_pos.right.x, this.mowers[mower].mow_pos.right.y );
#endif // MOWER_PRINT

    this.mowers[mower].new_pos = this.mowers[mower].mow_pos.left;
    this.mowers[mower].new_pos.x += ( this.mowers[mower].mow_pos.right.x - this.mowers[mower].mow_pos.left.x ) / 2.0;
    this.mowers[mower].new_pos.y += ( this.mowers[mower].mow_pos.right.y - this.mowers[mower].mow_pos.left.y ) / 2.0;

    this.mowers[mower].new_pos.x /= (double)this.pixelscale;
    this.mowers[mower].new_pos.y /= (double)this.pixelscale;

#ifdef MOWER_PRINT
    printf("move mower[%d], left: %d, right: %d\n", mower, left, right);
    printf("move mower[%d], pos: %f %f\n", mower, this.mowers[mower].new_pos.x, this.mowers[mower].new_pos.y);
#endif // MOWER_PRINT

    draw_mower(this.drawing, mower);

    gdk_threads_leave();
}

static void draw_mower(GtkWidget *widget, int nbr)
{
    GdkRectangle mower;
    static GdkPoint point = { .x = 0, .y = 0 };
    GdkGC* gc;

    mower.x = 0;
    mower.y = 0;
    mower.width = MOWER_SIDE_INT;
    mower.height = MOWER_SIDE_INT;

#ifdef MOWER_PRINT
    printf("Mower old: %d %d\n",
           this.mowers[nbr].current_pos.x,
           this.mowers[nbr].current_pos.y);
#endif // MOWER_PRINT

    mower.x = this.mowers[nbr].current_pos.x - mower.width/2;
    mower.y = this.mowers[nbr].current_pos.y - mower.height/2;


    gc = widget->style->fg_gc[GTK_STATE_NORMAL];
    draw_rectangle(widget, gc, &mower);
    draw_point(widget, gc, &point);

    this.mowers[nbr].current_pos.x = this.mowers[nbr].new_pos.x;
    this.mowers[nbr].current_pos.y = this.mowers[nbr].new_pos.y;

    mower.x = this.mowers[nbr].current_pos.x - mower.width/2;
    mower.y = this.mowers[nbr].current_pos.y - mower.height/2;

#if 1
    point.x = this.mowers[nbr].current_pos.x + cos(-this.mowers[nbr].mow_pos.direction) * MOWER_SIDE / 2.0;
    point.y = this.mowers[nbr].current_pos.y - sin(-this.mowers[nbr].mow_pos.direction) * MOWER_SIDE / 2.0;
#endif


#ifdef MOWER_PRINT
    printf("Mower new: %d %d\n",
           this.mowers[nbr].current_pos.x,
           this.mowers[nbr].current_pos.y);
#endif // MOWER_PRINT

    gc = widget->style->black_gc;
    draw_rectangle(widget, gc, &mower);

    gc = widget->style->bg_gc[GTK_STATE_NORMAL];
    draw_point(widget, gc, &point);
}

static void add_obstacle ( GtkWidget *widget, GdkGC* gc, gdouble x, gdouble y)
{
    GdkRectangle* obstacle;
    GtkTreeIter iter;

    obstacle = malloc(sizeof(*obstacle));

    obstacle->x = x;
    obstacle->y = y;
    obstacle->width = 30;
    obstacle->height = 30;

    gtk_list_store_append(this.liststore, &iter);
    gtk_list_store_set(this.liststore, &iter,
                       COLUMN_NAME, "obstacle",
                       COLUMN_X, obstacle->x,
                       COLUMN_Y, obstacle->y,
                       COLUMN_WIDTH, obstacle->width,
                       COLUMN_HEIGHT, obstacle->height,
                       -1);
    this.obstacle_list = g_list_append(this.obstacle_list, obstacle);


    draw_rectangle(widget, gc, obstacle);
}

static void range_sensor_changed_event(GtkComboBoxText* widget, gpointer data)
{
    gchar*  text;

    text = gtk_combo_box_text_get_active_text(widget);

    if ( text == NULL )
        return;

    this.range_sensor = table_sensor_name_to_value(text, range_sensors);

    g_free(text);
}

static void bwf_sensor_changed_event(GtkComboBoxText* widget, gpointer data)
{
    gchar*  text;

    text = gtk_combo_box_text_get_active_text(widget);

    if ( text == NULL )
        return;

    this.bwf_sensor = table_sensor_name_to_value(text, bwf_sensors);

    g_free(text);
}

static void voltage_sensor_changed_event(GtkComboBoxText* widget, gpointer data)
{
    gchar*  text;

    text = gtk_combo_box_text_get_active_text(widget);

    if ( text == NULL )
        return;

    this.voltage_sensor = table_sensor_name_to_value(text, voltage_sensors);

    g_free(text);
}

static void moisture_sensor_changed_event(GtkComboBoxText* widget, gpointer data)
{
    gchar*  text;

    text = gtk_combo_box_text_get_active_text(widget);

    if ( text == NULL )
        return;

    this.moisture_sensor = table_sensor_name_to_value(text, moisture_sensors);

    g_free(text);
}

static void pixelscale_changed_event(GtkComboBoxText* widget, gpointer data)
{
    gchar*  text;

    text = gtk_combo_box_text_get_active_text(widget);

    if ( text == NULL )
        return;

    this.pixelscale = atoi(text);

    g_free(text);
}

static void maxspeed_changed_event(GtkComboBoxText* widget, gpointer data)
{
    gchar*  text;

    text = gtk_combo_box_text_get_active_text(widget);

    if ( text == NULL )
        return;

    this.maxspeed = atoi(text);

    g_free(text);
}

static void voltage_changed_event(GtkComboBoxText* widget, gpointer data)
{
    gchar*  text;
    char*   c;
    int     high = 0;
    int     low = 0;

    text = gtk_combo_box_text_get_active_text(widget);

    if ( text == NULL )
        return;

    high = atoi(text);

    c = strchr(text, '.');
    if ( c == NULL )
        return;
    c++;

    low = atoi(c);

    this.voltage = high * 1000 + low * 100;

    g_free(text);
}

static void moisture_changed_event(GtkComboBoxText* widget, gpointer data)
{
    gchar*  text;

    text = gtk_combo_box_text_get_active_text(widget);

    if ( text == NULL )
        return;

    this.moisture = atoi(text);

    g_free(text);
}

static void treeview_changed_event(GtkTreeSelection *widget, gpointer data)
{
    GtkTreeIter iter;
    GtkTreeModel *model;
    GtkTreePath *path;
    gchar *type;
    gint x;
    gint y;
    gint width;
    gint height;


    if ( gtk_tree_selection_get_selected(widget, &model, &iter) ) {
        gtk_tree_model_get(model, &iter,
                           COLUMN_NAME, &type,
                           COLUMN_X, &x,
                           COLUMN_Y, &y,
                           COLUMN_WIDTH, &width,
                           COLUMN_HEIGHT, &height,
                           -1);

        if ( this.marked_item.type != TYPE_NONE )
            draw_rectangle(this.window, this.window->style->black_gc, this.marked_item.item);

        this.marked_item.iter = iter;
        this.marked_item.type = TYPE_NONE;

        if ( strcmp(type, "bwf") == 0 ) {
            GdkRectangle* bwf;
            bwf = find_bwf(x, y, width, height);
            this.marked_item.item = bwf;
            if ( bwf != NULL ) {
                this.marked_item.type = TYPE_BWF;
                draw_rectangle(this.window, this.window->style->white_gc, bwf);
            }
        }
        if ( strcmp(type, "obstacle") == 0 ) {
            GdkRectangle* obstacle;
            obstacle = find_obstacle(x, y, width, height);
            this.marked_item.item = obstacle;
            if ( obstacle != NULL ) {
                this.marked_item.type = TYPE_OBSTACLE;
                draw_rectangle(this.window, this.window->style->white_gc, obstacle);
            }
        }
        g_free(type);
    }
}

static void delete_marked_item()
{
    if ( this.marked_item.type != TYPE_NONE ) {
        switch ( this.marked_item.type ) {
            case TYPE_BWF:
                gtk_list_store_remove(this.liststore, &this.marked_item.iter);
                remove_bwf(this.marked_item.item);
                gtk_widget_queue_resize(this.drawing);
                break;

            case TYPE_OBSTACLE:
                gtk_list_store_remove(this.liststore, &this.marked_item.iter);
                remove_obstacle(this.marked_item.item);
                gtk_widget_queue_resize(this.drawing);
                break;
        }
    }
    this.marked_item.type = TYPE_NONE;
}

static void debug_button_clicked_event(GtkButton *widget, gpointer data)
{
    GtkLabel* label;
    const gchar* text;
    gboolean state;
    int value;

    state = gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(widget));

    if ( state == TRUE )
        value = 1;
    else
        value = 0;

    text = gtk_button_get_label(widget);

    if ( strcmp(text, BUTTON_COMMANDS) == 0)
        this.debug_commands = value;
    if ( strcmp(text, BUTTON_READ_VALUES) == 0)
        this.debug_values = value;
    if ( strcmp(text, BUTTON_RIGHT_MOTOR_INVERTED) == 0)
        this.right_motor_inverted = value;

    mower_net_debug(this.debug_commands, this.debug_values);
    mower_set_motors(this.right_motor_inverted);
}

static void button_clicked_event(GtkButton *widget, gpointer data)
{
    GtkLabel* label;
    const gchar* text;

    label = (GtkLabel*)this.label;
    text = gtk_button_get_label(widget);

    if ( strcmp(text, "Obstacle") == 0)
        gtk_label_set_text(label, text);
    if ( strcmp(text, "BWF") == 0)
        gtk_label_set_text(label, text);
    if ( strcmp(text, "Delete") == 0)
        delete_marked_item();
}

static void button_exit_event(GtkButton *widget, gpointer data)
{
  gtk_main_quit();
}

static void button_save_event(GtkButton *widget, gpointer data)
{
    char    buffer[256];
    char*   text;
    GList*  current;

    FILE*   file;

    file = fopen(PURPLESIM_SAVE_FILE, "w");

    if ( file == NULL ) {
        perror("Failed to save");
        return;
    }

    text = gtk_combo_box_text_get_active_text(GTK_COMBO_BOX_TEXT(this.cbox_sensor_range));
    snprintf(buffer, sizeof(buffer), SAVE_STRING_SENSOR_RANGE "%s\n", text);
    g_free(text);
    fwrite(buffer, sizeof(buffer[0]), strlen(buffer), file);
#ifdef SAVE_PRINT
    printf("%s", buffer);
#endif // SAVE_PRINT

    text = gtk_combo_box_text_get_active_text(GTK_COMBO_BOX_TEXT(this.cbox_sensor_bwf));
    snprintf(buffer, sizeof(buffer), SAVE_STRING_SENSOR_BWF "%s\n", text);
    g_free(text);
    fwrite(buffer, sizeof(buffer[0]), strlen(buffer), file);
#ifdef SAVE_PRINT
    printf("%s", buffer);
#endif // SAVE_PRINT

    text = gtk_combo_box_text_get_active_text(GTK_COMBO_BOX_TEXT(this.cbox_sensor_voltage));
    snprintf(buffer, sizeof(buffer), SAVE_STRING_SENSOR_VOLTAGE "%s\n", text);
    g_free(text);
    fwrite(buffer, sizeof(buffer[0]), strlen(buffer), file);
#ifdef SAVE_PRINT
    printf("%s", buffer);
#endif // SAVE_PRINT

    text = gtk_combo_box_text_get_active_text(GTK_COMBO_BOX_TEXT(this.cbox_sensor_moisture));
    snprintf(buffer, sizeof(buffer), SAVE_STRING_SENSOR_MOISTURE "%s\n", text);
    g_free(text);
    fwrite(buffer, sizeof(buffer[0]), strlen(buffer), file);
#ifdef SAVE_PRINT
    printf("%s", buffer);
#endif // SAVE_PRINT

    text = gtk_combo_box_text_get_active_text(GTK_COMBO_BOX_TEXT(this.cbox_voltage));
    snprintf(buffer, sizeof(buffer), SAVE_STRING_VOLTAGE "%s\n", text);
    g_free(text);
    fwrite(buffer, sizeof(buffer[0]), strlen(buffer), file);
#ifdef SAVE_PRINT
    printf("%s", buffer);
#endif // SAVE_PRINT

    text = gtk_combo_box_text_get_active_text(GTK_COMBO_BOX_TEXT(this.cbox_moisture));
    snprintf(buffer, sizeof(buffer), SAVE_STRING_MOISTURE "%s\n", text);
    g_free(text);
    fwrite(buffer, sizeof(buffer[0]), strlen(buffer), file);
#ifdef SAVE_PRINT
    printf("%s", buffer);
#endif // SAVE_PRINT

    text = gtk_combo_box_text_get_active_text(GTK_COMBO_BOX_TEXT(this.cbox_pixelscale));
    snprintf(buffer, sizeof(buffer), SAVE_STRING_PIXELSCALE "%s\n", text);
    g_free(text);
    fwrite(buffer, sizeof(buffer[0]), strlen(buffer), file);
#ifdef SAVE_PRINT
    printf("%s", buffer);
#endif // SAVE_PRINT

    text = gtk_combo_box_text_get_active_text(GTK_COMBO_BOX_TEXT(this.cbox_maxspeed));
    snprintf(buffer, sizeof(buffer), SAVE_STRING_MAXSPEED "%s\n", text);
    g_free(text);
    fwrite(buffer, sizeof(buffer[0]), strlen(buffer), file);
#ifdef SAVE_PRINT
    printf("%s", buffer);
#endif // SAVE_PRINT

    current = g_list_first(this.obstacle_list);
    while ( current != NULL ) {
        snprintf(buffer, sizeof(buffer), SAVE_STRING_OBSTACLE "%d %d %d %d\n",
                ((GdkRectangle*)(current->data))->x,
                ((GdkRectangle*)(current->data))->y,
                ((GdkRectangle*)(current->data))->width,
                ((GdkRectangle*)(current->data))->height);
        fwrite(buffer, sizeof(buffer[0]), strlen(buffer), file);
#ifdef SAVE_PRINT
        printf("%s", buffer);
#endif // SAVE_PRINT
        current = g_list_next(current);
    }

    current = g_list_first(this.bwf_list);
    while ( current != NULL ) {
        snprintf(buffer, sizeof(buffer), SAVE_STRING_BWF "%d %d %d %d\n",
                ((GdkRectangle*)(current->data))->x,
                ((GdkRectangle*)(current->data))->y,
                ((GdkRectangle*)(current->data))->width,
                ((GdkRectangle*)(current->data))->height);
        fwrite(buffer, sizeof(buffer[0]), strlen(buffer), file);
#ifdef SAVE_PRINT
        printf("%s", buffer);
#endif // SAVE_PRINT
        current = g_list_next(current);
    }

    fclose(file);
}

static void button_load_event(GtkButton *widget, gpointer data)
{
    char    buffer[256];
    char*   b;
    char*   c;
    int     i;
    int     a[4];

    FILE*   file;

    file = fopen(PURPLESIM_SAVE_FILE, "r");

    if ( file == NULL ) {
        perror("Failed to load");
        return;
    }

    while ( b = fgets(buffer, sizeof(buffer), file) ) {
        if ( strncmp(buffer, SAVE_STRING_SENSOR_RANGE, strlen(SAVE_STRING_SENSOR_RANGE)) == 0 ) {
            b += strlen(SAVE_STRING_SENSOR_RANGE);
            c = strchr(b, '\n');
            if ( c != NULL )
                *c = '\0';
        } else
        if ( strncmp(buffer, SAVE_STRING_SENSOR_BWF, strlen(SAVE_STRING_SENSOR_BWF)) == 0 ) {
            b += strlen(SAVE_STRING_SENSOR_BWF);
            c = strchr(b, '\n');
            if ( c != NULL )
                *c = '\0';
        } else
        if ( strncmp(buffer, SAVE_STRING_SENSOR_VOLTAGE, strlen(SAVE_STRING_SENSOR_VOLTAGE)) == 0 ) {
            b += strlen(SAVE_STRING_SENSOR_VOLTAGE);
            c = strchr(b, '\n');
            if ( c != NULL )
                *c = '\0';
        } else
        if ( strncmp(buffer, SAVE_STRING_SENSOR_MOISTURE, strlen(SAVE_STRING_SENSOR_MOISTURE)) == 0 ) {
            b += strlen(SAVE_STRING_SENSOR_MOISTURE);
            c = strchr(b, '\n');
            if ( c != NULL )
                *c = '\0';
        } else
        if ( strncmp(buffer, SAVE_STRING_VOLTAGE, strlen(SAVE_STRING_VOLTAGE)) == 0 ) {
            b += strlen(SAVE_STRING_VOLTAGE);
            c = strchr(b, '\n');
            if ( c != NULL )
                *c = '\0';
        } else
        if ( strncmp(buffer, SAVE_STRING_MOISTURE, strlen(SAVE_STRING_MOISTURE)) == 0 ) {
            b += strlen(SAVE_STRING_MOISTURE);
            c = strchr(b, '\n');
            if ( c != NULL )
                *c = '\0';
        } else
        if ( strncmp(buffer, SAVE_STRING_PIXELSCALE, strlen(SAVE_STRING_PIXELSCALE)) == 0 ) {
            b += strlen(SAVE_STRING_PIXELSCALE);
            c = strchr(b, '\n');
            if ( c != NULL )
                *c = '\0';
        } else
        if ( strncmp(buffer, SAVE_STRING_MAXSPEED, strlen(SAVE_STRING_MAXSPEED)) == 0 ) {
            b += strlen(SAVE_STRING_MAXSPEED);
            c = strchr(b, '\n');
            if ( c != NULL )
                *c = '\0';
        } else
        if ( strncmp(buffer, SAVE_STRING_OBSTACLE, strlen(SAVE_STRING_OBSTACLE)) == 0 ) {
            b += strlen(SAVE_STRING_OBSTACLE);
            for ( i = 0; b != NULL && i < 4; i++ ) {
                a[i] = atoi(b);
                b = strchr(b, ' ');
                if ( b != NULL )
                    b++;
            }
            if ( i == 4 )
                add_obstacle( this.drawing, this.drawing->style->black_gc, a[0], a[1]);
        } else
        if ( strncmp(buffer, SAVE_STRING_BWF, strlen(SAVE_STRING_BWF)) == 0 ) {
            b += strlen(SAVE_STRING_BWF);
            for ( i = 0; b != NULL && i < 4; i++ ) {
                a[i] = atoi(b);
                b = strchr(b, ' ');
                if ( b != NULL )
                    b++;
            }
            if ( i == 4 )
            {
                add_bwf( this.drawing, this.drawing->style->black_gc, a[0], a[1], a[2], a[3]);
            }
        }
    }

    fclose(file);
}

static void button_refresh_event(GtkButton *widget, gpointer data)
{
    gtk_widget_queue_resize(this.drawing);
}

static gint button_press_event (GtkWidget *widget, GdkEventButton *event, gpointer data)
{
    GtkLabel* label;

    label = (GtkLabel*)this.label;

    if ( strcmp(gtk_label_get_text(label), "Obstacle") == 0 )
    {
        if (event->button == 1 && pixmap != NULL )
            add_obstacle( widget, widget->style->black_gc, event->x, event->y);
    }
    if ( strcmp(gtk_label_get_text(label), "BWF") == 0 )
    {
        if (event->button == 1 && pixmap != NULL )
            add_bwf( widget, widget->style->black_gc, event->x, event->y, -1, -1);
    }

    return TRUE;
}

static gint
expose_event (GtkWidget *widget, GdkEventExpose *event)
{
    gdk_draw_pixmap(widget->window,
                    widget->style->fg_gc[GTK_WIDGET_STATE (widget)],
                    pixmap,
                    event->area.x, event->area.y,
                    event->area.x, event->area.y,
                    event->area.width, event->area.height);

    return FALSE;
}

static void print_help(char* argv[])
{
    printf("Usage: %s [OPTION...]\n", basename(argv[0]));
    printf("\n");
    printf("Options:\n");
    printf("\n");
    printf("  -p <port>      use port (default: %d)\n", DEFAULT_PORT);
    printf("  -h             show this help\n");
    printf("\n");
}

static void parse_options(int argc, char* argv[], struct options* opt)
{
    char c;
    while ( (c = getopt(argc, argv, "p:h")) != -1 ) {
        switch (c) {
            case 'p':
                if ( atoi(optarg) > 65535 ) {
                    fprintf(stderr, "Error: Port %s too high\n", optarg);
                    exit(1);
                } else
                    snprintf(opt->port, sizeof(opt->port), "%s", optarg);
                break;
            case 'h':
                print_help(argv);
                exit(1);
                break;
            default:
                break;
        }
    }
}

int main(int argc, char *argv[])
{
    int         i;
    char        buffer[256];
    struct options  opt;

    memset(&opt, 0, sizeof(opt));
    parse_options(argc, argv, &opt);

    GtkCellRenderer *renderer;
    GtkTreeViewColumn *column;
    GtkTreeSelection *selection;

    GtkWidget*  scrollwindow1;
    GtkWidget*  button1;
    GtkWidget*  button2;
    GtkWidget*  button3;
    GtkWidget*  button_load;
    GtkWidget*  button_save;
    GtkWidget*  button_refresh;
    GtkWidget*  button_exit;
    GtkWidget*  cbox_sensor_bwf;
    GtkWidget*  cbox_pixelscale;
    GtkWidget*  cbox_maxspeed;
    GtkWidget*  cbox_voltage;
    GtkWidget*  cbox_moisture;
//    GtkWidget*  bbox1;
    GtkWidget*  bbox2;
    GtkWidget*  bbox3;
    GtkWidget*  bbox4;
    GtkWidget*  bbox5;
    GtkWidget*  bbox6;
    GtkWidget*  hbox1;
    GtkWidget*  vbox1;
    GtkWidget*  vbox2;
    GtkWidget*  cbutton1;
    GtkWidget*  cbutton2;
    GtkWidget*  cbutton3;

    gdk_threads_init();
    gtk_init(&argc, &argv);

    this.marked_item.type = TYPE_NONE;
    for ( i = 0; i < MAX_MOWERS; i++ ) {
        initialize_mower(i);
    }

    this.pixelscale = 10;
    this.maxspeed = 30;
    this.voltage = 12000;
    this.moisture = 40;

    this.debug_commands = 0;
    this.debug_values = 0;
    this.right_motor_inverted = 0;

    this.window = gtk_window_new (GTK_WINDOW_TOPLEVEL);
    this.drawing = gtk_drawing_area_new();
    vbox1 = gtk_vbox_new(FALSE, 0);
    vbox2 = gtk_vbox_new(FALSE, 0);
    hbox1 = gtk_hbox_new(FALSE, 0);
//    bbox1 = gtk_hbutton_box_new();
    bbox2 = gtk_hbutton_box_new();
    bbox3 = gtk_hbutton_box_new();
    bbox4 = gtk_hbutton_box_new();
    bbox5 = gtk_hbutton_box_new();
    bbox6 = gtk_hbutton_box_new();
    this.label = gtk_label_new("BWF");
    this.treeview1 = gtk_tree_view_new();
    button1 = gtk_button_new_with_label("Obstacle");
    button2 = gtk_button_new_with_label("BWF");
    button3 = gtk_button_new_with_label("Delete");
    button_save = gtk_button_new_with_label("Save");
    button_refresh = gtk_button_new_with_label("Refresh");
    button_load = gtk_button_new_with_label("Load");
    button_exit = gtk_button_new_with_label("Exit");
    cbutton1 = gtk_check_button_new_with_label(BUTTON_COMMANDS);
    cbutton2 = gtk_check_button_new_with_label(BUTTON_READ_VALUES);
    cbutton3 = gtk_check_button_new_with_label(BUTTON_RIGHT_MOTOR_INVERTED);
    this.cbox_sensor_range = gtk_combo_box_text_new();
    this.cbox_sensor_bwf = gtk_combo_box_text_new();
    this.cbox_sensor_voltage = gtk_combo_box_text_new();
    this.cbox_sensor_moisture = gtk_combo_box_text_new();
    this.cbox_pixelscale = gtk_combo_box_text_new();
    this.cbox_maxspeed = gtk_combo_box_text_new();
    this.cbox_voltage = gtk_combo_box_text_new();
    this.cbox_moisture = gtk_combo_box_text_new();
//    button[0] = gtk_check_button_new_with_label("Obstacle");
//    button[1] = gtk_check_button_new_with_label("BWF");
    this.liststore = gtk_list_store_new(N_COLUMNS, G_TYPE_STRING, G_TYPE_INT, G_TYPE_INT, G_TYPE_INT, G_TYPE_INT);
    scrollwindow1 = gtk_scrolled_window_new(NULL, NULL);
    this.bwf_list = NULL;
    this.obstacle_list = NULL;

    // init tree view
    gtk_tree_view_set_headers_visible(GTK_TREE_VIEW(this.treeview1), TRUE);
    renderer = gtk_cell_renderer_text_new();
    column = gtk_tree_view_column_new_with_attributes("Name",
                      renderer,
                      "text", COLUMN_NAME,
                      NULL);
    gtk_tree_view_append_column(GTK_TREE_VIEW(this.treeview1), column);
    column = gtk_tree_view_column_new_with_attributes("X",
                      renderer,
                      "text", COLUMN_X,
                      NULL);
    gtk_tree_view_append_column(GTK_TREE_VIEW(this.treeview1), column);
    column = gtk_tree_view_column_new_with_attributes("Y",
                      renderer,
                      "text", COLUMN_Y,
                      NULL);
    gtk_tree_view_append_column(GTK_TREE_VIEW(this.treeview1), column);
    column = gtk_tree_view_column_new_with_attributes("Width",
                      renderer,
                      "text", COLUMN_WIDTH,
                      NULL);
    gtk_tree_view_append_column(GTK_TREE_VIEW(this.treeview1), column);
    column = gtk_tree_view_column_new_with_attributes("Height",
                      renderer,
                      "text", COLUMN_HEIGHT,
                      NULL);
    gtk_tree_view_append_column(GTK_TREE_VIEW(this.treeview1), column);

    gtk_tree_view_set_model(GTK_TREE_VIEW(this.treeview1),
                            GTK_TREE_MODEL(this.liststore));

    for( i = 0; strcmp(range_sensors[i].name, ""); i++ )
        gtk_combo_box_text_append_text(GTK_COMBO_BOX_TEXT(this.cbox_sensor_range), range_sensors[i].name);
    gtk_combo_box_set_active(GTK_COMBO_BOX(this.cbox_sensor_range), 0);
    this.range_sensor = range_sensors[0].sensor;

    for( i = 0; strcmp(bwf_sensors[i].name, ""); i++ )
        gtk_combo_box_text_append_text(GTK_COMBO_BOX_TEXT(this.cbox_sensor_bwf), bwf_sensors[i].name);
    gtk_combo_box_set_active(GTK_COMBO_BOX(this.cbox_sensor_bwf), 0);
    this.bwf_sensor = bwf_sensors[0].sensor;

    for( i = 0; strcmp(voltage_sensors[i].name, ""); i++ )
        gtk_combo_box_text_append_text(GTK_COMBO_BOX_TEXT(this.cbox_sensor_voltage), voltage_sensors[i].name);
    gtk_combo_box_set_active(GTK_COMBO_BOX(this.cbox_sensor_voltage), 0);
    this.voltage_sensor = voltage_sensors[0].sensor;

    for( i = 0; strcmp(moisture_sensors[i].name, ""); i++ )
        gtk_combo_box_text_append_text(GTK_COMBO_BOX_TEXT(this.cbox_sensor_moisture), moisture_sensors[i].name);
    gtk_combo_box_set_active(GTK_COMBO_BOX(this.cbox_sensor_moisture), 0);
    this.moisture_sensor = moisture_sensors[0].sensor;

    for( i = 10; i > 0 ; i-- ) {
        snprintf(buffer, sizeof(buffer), "%d cm/pixel", i);
        gtk_combo_box_text_append_text(GTK_COMBO_BOX_TEXT(this.cbox_pixelscale), buffer);
    }
    gtk_combo_box_set_active(GTK_COMBO_BOX(this.cbox_pixelscale), 0);

    for( i = 10; i > 0 ; i-- ) {
        snprintf(buffer, sizeof(buffer), "%d cm/s", i*3);
        gtk_combo_box_text_append_text(GTK_COMBO_BOX_TEXT(this.cbox_maxspeed), buffer);
    }
    gtk_combo_box_set_active(GTK_COMBO_BOX(this.cbox_maxspeed), 0);

    for( i = 0; i <= 10 ; i++ ) {
        snprintf(buffer, sizeof(buffer), "%d.%d V", 12 - ((i+4)/5), (10-(i%5)*2)%10);
        gtk_combo_box_text_append_text(GTK_COMBO_BOX_TEXT(this.cbox_voltage), buffer);
    }
    gtk_combo_box_set_active(GTK_COMBO_BOX(this.cbox_voltage), 0);

    for( i = 10; i >= 0 ; i-- ) {
        snprintf(buffer, sizeof(buffer), "%d %%", i*10);
        gtk_combo_box_text_append_text(GTK_COMBO_BOX_TEXT(this.cbox_moisture), buffer);
    }
    gtk_combo_box_set_active(GTK_COMBO_BOX(this.cbox_moisture), 6);


//    gtk_toggle_button_set_mode(button[0], FALSE);
//    gtk_toggle_button_set_mode(button[1], FALSE);

    gtk_scrolled_window_set_policy(GTK_SCROLLED_WINDOW(scrollwindow1), GTK_POLICY_NEVER, GTK_POLICY_AUTOMATIC);

    GdkColor color_green;
    gdk_color_parse("#00b008", &color_green);

    GdkColor color_red;
    gdk_color_parse("#ff0000", &color_red);

    gtk_widget_modify_fg(this.drawing, GTK_STATE_NORMAL, &color_green);
    gtk_widget_modify_bg(this.drawing, GTK_STATE_NORMAL, &color_red);

    gtk_container_add(GTK_CONTAINER (this.window), hbox1);

    gtk_container_add(GTK_CONTAINER (hbox1), vbox1);
    gtk_container_add(GTK_CONTAINER (hbox1), vbox2);

    gtk_container_add(GTK_CONTAINER (vbox1), this.drawing);
//    gtk_container_add(GTK_CONTAINER (vbox1), bbox1);
    gtk_container_add(GTK_CONTAINER (vbox1), bbox2);
    gtk_container_add(GTK_CONTAINER (vbox1), bbox3);
    gtk_container_add(GTK_CONTAINER (vbox1), bbox4);
    gtk_container_add(GTK_CONTAINER (vbox1), bbox5);
    gtk_container_add(GTK_CONTAINER (vbox1), bbox6);

    gtk_container_add(GTK_CONTAINER (bbox2), this.label);

    gtk_container_add(GTK_CONTAINER (bbox2), button1);
    gtk_container_add(GTK_CONTAINER (bbox2), button2);
    gtk_container_add(GTK_CONTAINER (bbox2), button3);

    gtk_container_add(GTK_CONTAINER (bbox3), this.cbox_sensor_range);
    gtk_container_add(GTK_CONTAINER (bbox3), this.cbox_sensor_bwf);
    gtk_container_add(GTK_CONTAINER (bbox3), this.cbox_sensor_voltage);
    gtk_container_add(GTK_CONTAINER (bbox3), this.cbox_sensor_moisture);

    gtk_container_add(GTK_CONTAINER (bbox4), this.cbox_voltage);
    gtk_container_add(GTK_CONTAINER (bbox4), this.cbox_moisture);
    gtk_container_add(GTK_CONTAINER (bbox4), this.cbox_pixelscale);
    gtk_container_add(GTK_CONTAINER (bbox4), this.cbox_maxspeed);

    gtk_container_add(GTK_CONTAINER (bbox5), button_save);
    gtk_container_add(GTK_CONTAINER (bbox5), button_load);
    gtk_container_add(GTK_CONTAINER (bbox5), button_refresh);
    gtk_container_add(GTK_CONTAINER (bbox5), button_exit);

    gtk_container_add(GTK_CONTAINER (bbox6), cbutton1);
    gtk_container_add(GTK_CONTAINER (bbox6), cbutton2);
    gtk_container_add(GTK_CONTAINER (bbox6), cbutton3);

    gtk_container_add(GTK_CONTAINER (vbox2), scrollwindow1);
    gtk_container_add(GTK_CONTAINER (scrollwindow1), this.treeview1);
//    gtk_container_add(GTK_CONTAINER (bbox1), button[0]);
//    gtk_container_add(GTK_CONTAINER (bbox1), button[1]);

    gtk_widget_set_size_request(this.drawing, DEFAULT_LAWN_SIZE_X, DEFAULT_LAWN_SIZE_Y);

    g_signal_connect(G_OBJECT(this.drawing), "configure_event",
                     G_CALLBACK(configure_event), NULL);

    g_signal_connect(G_OBJECT(this.drawing), "expose_event",
                     G_CALLBACK(expose_event), NULL);

    g_signal_connect(G_OBJECT(this.drawing), "button_press_event",
                     G_CALLBACK(button_press_event), NULL);

    g_signal_connect(G_OBJECT(button1), "clicked",
                     G_CALLBACK(button_clicked_event), NULL);

    g_signal_connect(G_OBJECT(button2), "clicked",
                     G_CALLBACK(button_clicked_event), NULL);

    g_signal_connect(G_OBJECT(button3), "clicked",
                     G_CALLBACK(button_clicked_event), NULL);

    g_signal_connect(G_OBJECT(cbutton1), "clicked",
                     G_CALLBACK(debug_button_clicked_event), NULL);

    g_signal_connect(G_OBJECT(cbutton2), "clicked",
                     G_CALLBACK(debug_button_clicked_event), NULL);

    g_signal_connect(G_OBJECT(cbutton3), "clicked",
                     G_CALLBACK(debug_button_clicked_event), NULL);

    g_signal_connect(G_OBJECT(button_save), "clicked",
                     G_CALLBACK(button_save_event), NULL);

    g_signal_connect(G_OBJECT(button_load), "clicked",
                     G_CALLBACK(button_load_event), NULL);

    g_signal_connect(G_OBJECT(button_refresh), "clicked",
                     G_CALLBACK(button_refresh_event), NULL);

    g_signal_connect(G_OBJECT(button_exit), "clicked",
                     G_CALLBACK(button_exit_event), NULL);

    selection = gtk_tree_view_get_selection(GTK_TREE_VIEW(this.treeview1));

    g_signal_connect(G_OBJECT(selection), "changed",
                     G_CALLBACK(treeview_changed_event), NULL);

    g_signal_connect(G_OBJECT(this.cbox_sensor_range), "changed",
                     G_CALLBACK(range_sensor_changed_event), NULL);

    g_signal_connect(G_OBJECT(this.cbox_sensor_bwf), "changed",
                     G_CALLBACK(bwf_sensor_changed_event), NULL);

    g_signal_connect(G_OBJECT(this.cbox_sensor_voltage), "changed",
                     G_CALLBACK(voltage_sensor_changed_event), NULL);

    g_signal_connect(G_OBJECT(this.cbox_sensor_moisture), "changed",
                     G_CALLBACK(moisture_sensor_changed_event), NULL);

    g_signal_connect(G_OBJECT(this.cbox_pixelscale), "changed",
                     G_CALLBACK(pixelscale_changed_event), NULL);

    g_signal_connect(G_OBJECT(this.cbox_maxspeed), "changed",
                     G_CALLBACK(maxspeed_changed_event), NULL);

    g_signal_connect(G_OBJECT(this.cbox_voltage), "changed",
                     G_CALLBACK(voltage_changed_event), NULL);

    g_signal_connect(G_OBJECT(this.cbox_moisture), "changed",
                     G_CALLBACK(moisture_changed_event), NULL);

    gtk_widget_set_events(this.drawing,
                          GDK_EXPOSURE_MASK
                          | GDK_BUTTON_PRESS_MASK );

    gtk_widget_show_all(this.window);

    mower_net(opt.port);
    mower_net_debug(this.debug_commands, this.debug_values);
    mower_set_motors(this.right_motor_inverted);

    gtk_main();

    return 0;
}

