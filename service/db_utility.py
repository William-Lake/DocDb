import logging
from peewee import *
from service.database import *

class DatabaseUtility(object):

    def __init__(self):

        self.logger = logging.getLogger()

        self.logger.info('DatabaseUtility Loaded')

    def get_saved_doc_names(self): return [Doc.name for doc in Doc.select(Doc.name)]

    def get_doc_by_name(self,name): return Doc.select().where(Doc.name == name)

    def save_docs(self,name_data_dict): 

        for name in name_data_dict.keys(): Doc.create(name=name,data=name_data_dict[name])

        


   