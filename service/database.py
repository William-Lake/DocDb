import os
from peewee import *

db = SqliteDatabase(os.path.realpath(__file__).replace('database.py','DocDb.db'))

class BaseModel(Model):

    class Meta:

        database = db

class Doc(BaseModel):

    id = PrimaryKeyField(unique=True)

    name = TextField(null=False)

    data = BlobField(null=False)