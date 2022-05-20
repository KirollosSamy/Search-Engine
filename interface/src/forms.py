from email.policy import default
from flask_wtf import FlaskForm
from werkzeug.exceptions import Locked
from wtforms import StringField, PasswordField, SubmitField, IntegerField, FloatField, DateField, SelectField
from wtforms import validators
from wtforms.validators import Length, EqualTo, DataRequired,NumberRange

class Search_form(FlaskForm):
    Key = StringField(label="Key", validators=[DataRequired()])