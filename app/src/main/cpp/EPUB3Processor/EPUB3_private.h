#include <stdio.h>
#include <string.h>

#include <libxml/tree.h>
#include <libxml/parser.h>
#include <libxml/xmlreader.h>
#include <libxml/xpath.h>
#include <libxml/xpathInternals.h>
#include <sys/types.h>
#include <sys/stat.h>
#ifdef __linux__
#include <dirent.h>
#else
#include <sys/dirent.h>
#endif

#include <string.h>
#include <assert.h>
#include <errno.h>
#include <stdlib.h>
#include <dirent.h>
#include "EPUB3.h"
#include <unzip.h>

#ifndef EPUB3_private_h
#define EPUB3_private_h

#define EXPORT __attribute__((visibility("default")))

#pragma mark - Object Pointer Types

typedef struct EPUB3Metadata * EPUB3MetadataRef;
typedef struct EPUB3MetadataMetaItem * EPUB3MetadataMetaItemRef;
typedef struct EPUB3ManifestItem * EPUB3ManifestItemRef;
typedef struct EPUB3Manifest * EPUB3ManifestRef;
typedef struct EPUB3Spine * EPUB3SpineRef;
typedef struct EPUB3SpineItem * EPUB3SpineItemRef;
typedef struct EPUB3Toc * EPUB3TocRef;

const char * kEPUB3TypeID;
const char * kEPUB3MetadataTypeID;
const char * kEPUB3MetadataItemTypeID;
const char * kEPUB3ManifestTypeID;
const char * kEPUB3ManifestItemTypeID;
const char * kEPUB3SpineTypeID;
const char * kEPUB3SpineItemTypeID;
const char * kEPUB3TocTypeID;
const char * kEPUB3TocItemTypeID;


#pragma mark - Internal XML Parsing State

typedef enum {
  kEPUB3OPFStateRoot = 0,
  kEPUB3OPFStateMetadata,
  kEPUB3OPFStateManifest,
  kEPUB3OPFStateSpine, 
  kEPUB3NCXStateRoot,
  kEPUB3NCXStateNavMap,
} EPUB3XMLParseState;

typedef struct _EPUB3OPFParseContext {
  EPUB3XMLParseState state;
  const xmlChar *tagName;
  int32_t attributeCount;
  char ** attributes;
  EPUB3Bool shouldParseTextNode;
  void * userInfo;
} EPUB3XMLParseContext;

typedef EPUB3XMLParseContext * EPUB3XMLParseContextPtr;

#pragma mark - Type definitions

typedef struct EPUB3Type {
  const char *typeID;
  uint32_t refCount;
} EPUB3Type;

struct EPUB3Object {
  EPUB3Type _type;
};

typedef struct EPUB3Object *EPUB3ObjectRef;

typedef enum {
  kEPUB3Version_2 = 200,
  kEPUB3Version_3 = 300,
} EPUB3Version;

struct EPUB3 {
  EPUB3Type _type;
  EPUB3MetadataRef metadata;
  EPUB3ManifestRef manifest;
  EPUB3SpineRef spine;
  EPUB3TocRef toc;
  char * archivePath;
  unzFile archive;
  uint32_t archiveFileCount;
};

struct EPUB3MetadataMetaItem {
    EPUB3Type _type;
    char * name;
    char * content;
};

#define META_ITEM_HASH_SIZE 16

struct EPUB3Metadata {
    EPUB3Type _type;
    EPUB3Version version;
    EPUB3ManifestItemRef ncxItem;
    char * title;
    char * author;
    char * _uniqueIdentifierID;
    char * identifier;
    char * language;
    char * coverImageId;
    EPUB3MetadataMetaItemRef metaTable[META_ITEM_HASH_SIZE];
    int32_t itemCount;
};

struct EPUB3ManifestItem {
  EPUB3Type _type;
  char * itemId;
  char * href;
  char * mediaType;
  char * properties; // EPUB3
  char * requiredModules;
};

#define MANIFEST_HASH_SIZE 512

typedef struct EPUB3ManifestItemListItem {
  EPUB3ManifestItemRef item;
  struct EPUB3ManifestItemListItem * next;
} * EPUB3ManifestItemListItemPtr;

struct EPUB3Manifest {
  EPUB3Type _type;
  EPUB3ManifestItemListItemPtr itemTable[MANIFEST_HASH_SIZE];
  int32_t itemCount;
};

typedef struct EPUB3SpineItemListItem {
  EPUB3SpineItemRef item;
  struct EPUB3SpineItemListItem * next;
} * EPUB3SpineItemListItemPtr;

struct EPUB3Spine {
  EPUB3Type _type;
  int32_t itemCount;
  int32_t linearItemCount;
  EPUB3SpineItemListItemPtr head;
  EPUB3SpineItemListItemPtr tail;
};

struct EPUB3SpineItem {
  EPUB3Type _type;
  EPUB3Bool isLinear;
  char * idref;
  EPUB3ManifestItemRef manifestItem; //weak ref
};

typedef struct EPUB3TocItemChildListItem {
  EPUB3TocItemRef item;
  struct EPUB3TocItemChildListItem * next;
} * EPUB3TocItemChildListItemPtr;

struct EPUB3Toc {
  EPUB3Type _type;
  int32_t rootItemCount;
  EPUB3TocItemChildListItemPtr rootItemsHead;
  EPUB3TocItemChildListItemPtr rootItemsTail;
};

struct EPUB3TocItem {
  EPUB3Type _type;
  char * title;
  char * href;
  EPUB3TocItemRef parent; //weak ref
  int32_t childCount;
  EPUB3TocItemChildListItemPtr childrenHead;
  EPUB3TocItemChildListItemPtr childrenTail;
//  EPUB3ManifestItemRef manifestItem; //weak ref
};

#pragma mark - Base Object

void EPUB3ObjectRelease(void *object);
void EPUB3ObjectRetain(void *object);
void * EPUB3ObjectInitWithTypeID(void *object, const char *typeID);

#pragma mark - Main EPUB3 Object

EPUB3Ref EPUB3Create();
EPUB3Error EPUB3PrepareArchiveAtPath(EPUB3Ref epub, const char * path);
EPUB3Error EPUB3PrepareArchiveAtFd(EPUB3Ref epub, const int fd);
EPUB3Error EPUB3InitAndValidate(EPUB3Ref epub);
void EPUB3SetStringValue(char ** location, const char *value);
char * EPUB3CopyStringValue(char ** location);
EPUB3MetadataRef EPUB3CopyMetadata(EPUB3Ref epub);
void EPUB3SetMetadata(EPUB3Ref epub, EPUB3MetadataRef metadata);
void EPUB3SetManifest(EPUB3Ref epub, EPUB3ManifestRef manifest);
void EPUB3SetSpine(EPUB3Ref epub, EPUB3SpineRef spine);

#pragma mark - Metadata

EPUB3MetadataRef EPUB3MetadataCreate();
EPUB3MetadataMetaItemRef EPUB3MetadataItemCreate();
void EPUB3MetadataRetain(EPUB3MetadataRef metadata);
void EPUB3MetadataRelease(EPUB3MetadataRef metadata);
void EPUB3MetadataMetaItemRetain(EPUB3MetadataMetaItemRef item);
void EPUB3MetadataMetaItemRelease(EPUB3MetadataMetaItemRef item);
void EPUB3MetadataInsertItem(EPUB3MetadataRef metadata, EPUB3MetadataMetaItemRef item);
EPUB3MetadataMetaItemRef EPUB3MetadataCopyItemWithId(EPUB3MetadataRef metadata, const char * itemId);
EPUB3MetadataMetaItemRef EPUB3MetadataFindItemWithId(EPUB3MetadataRef metadata, const char * itemId);
void EPUB3MetadataSetNCXItem(EPUB3MetadataRef metadata, EPUB3ManifestItemRef ncxItem);
void EPUB3MetadataSetTitle(EPUB3MetadataRef metadata, const char * title);
void EPUB3MetadataSetAuthor(EPUB3MetadataRef metadata, const char * author);
void EPUB3MetadataSetIdentifier(EPUB3MetadataRef metadata, const char * identifier);
void EPUB3MetadataSetLanguage(EPUB3MetadataRef metadata, const char * language);
void EPUB3MetadataSetCoverImageId(EPUB3MetadataRef metadata, const char * coverImgId);

#pragma mark - Manifest

EPUB3ManifestRef EPUB3ManifestCreate();
EPUB3ManifestItemRef EPUB3ManifestItemCreate();
void EPUB3ManifestRetain(EPUB3ManifestRef manifest);
void EPUB3ManifestRelease(EPUB3ManifestRef manifest);
void EPUB3ManifestItemRetain(EPUB3ManifestItemRef item);
void EPUB3ManifestItemRelease(EPUB3ManifestItemRef item);
void EPUB3ManifestInsertItem(EPUB3ManifestRef manifest, EPUB3ManifestItemRef item);
EPUB3ManifestItemRef EPUB3ManifestCopyItemWithId(EPUB3ManifestRef manifest, const char * itemId);
EPUB3ManifestItemListItemPtr EPUB3ManifestFindItemWithId(EPUB3ManifestRef manifest, const char * itemId);

#pragma mark - Spine

EPUB3SpineRef EPUB3SpineCreate();
EPUB3SpineItemRef EPUB3SpineItemCreate();
void EPUB3SpineRetain(EPUB3SpineRef spine);
void EPUB3SpineRelease(EPUB3SpineRef spine);
void EPUB3SpineItemRetain(EPUB3SpineItemRef item);
void EPUB3SpineItemRelease(EPUB3SpineItemRef item);
void EPUB3SpineAppendItem(EPUB3SpineRef spine, EPUB3SpineItemRef item);
void EPUB3SpineItemSetManifestItem(EPUB3SpineItemRef spineItem, EPUB3ManifestItemRef manifestItem);

#pragma mark - Table of Contents

EPUB3TocRef EPUB3TocCreate();
EPUB3TocItemRef EPUB3TocItemCreate();
void EPUB3TocRetain(EPUB3TocRef toc);
void EPUB3TocRelease(EPUB3TocRef toc);
void EPUB3TocItemRetain(EPUB3TocItemRef item);
void EPUB3TocItemRelease(EPUB3TocItemRef item);
void EPUB3TocAddRootItem(EPUB3TocRef toc, EPUB3TocItemRef item);
void EPUB3TocItemAppendChild(EPUB3TocItemRef parent, EPUB3TocItemRef child);

#pragma mark - XML Parsing

EPUB3Error EPUB3InitFromOPF(EPUB3Ref epub, const char * opfFilename);
void EPUB3SaveParseContext(EPUB3XMLParseContextPtr *ctxPtr, EPUB3XMLParseState state, const xmlChar * tagName, int32_t attrCount, char ** attrs, EPUB3Bool shouldParseTextNode, void * userInfo);
void EPUB3PopAndFreeParseContext(EPUB3XMLParseContextPtr *contextPtr);
EPUB3Error EPUB3ProcessXMLReaderNodeForMetadataInOPF(EPUB3Ref epub, xmlTextReaderPtr reader, EPUB3XMLParseContextPtr *context);
EPUB3Error EPUB3ProcessXMLReaderNodeForManifestInOPF(EPUB3Ref epub, xmlTextReaderPtr reader, EPUB3XMLParseContextPtr *context);
EPUB3Error EPUB3ProcessXMLReaderNodeForSpineInOPF(EPUB3Ref epub, xmlTextReaderPtr reader, EPUB3XMLParseContextPtr *context);
EPUB3Error EPUB3ParseXMLReaderNodeForOPF(EPUB3Ref epub, xmlTextReaderPtr reader, EPUB3XMLParseContextPtr *currentContext);
EPUB3Error EPUB3ParseOPFFromData(EPUB3Ref epub, void * buffer, uint32_t bufferSize);

#pragma mark - NCX XML Parsing

EPUB3Error EPUB3ParseNCXFromData(EPUB3Ref epub, void * buffer, uint32_t bufferSize);
EPUB3Error EPUB3ParseXMLReaderNodeForNCX(EPUB3Ref epub, xmlTextReaderPtr reader, EPUB3XMLParseContextPtr *currentContext);
EPUB3Error EPUB3ProcessXMLReaderNodeForNavMapInNCX(EPUB3Ref epub, xmlTextReaderPtr reader, EPUB3XMLParseContextPtr *context);

#pragma mark - Validation

EPUB3Error EPUB3ValidateMimetype(EPUB3Ref epub);
EPUB3Error EPUB3ValidateFileExistsAndSeekInArchive(EPUB3Ref epub, const char * filename);

#pragma mark - File and Zip Functions

EPUB3Error EPUB3CopyFileIntoBuffer(EPUB3Ref epub, void **buffer, uint32_t *bufferSize, uint32_t *bytesCopied, const char * filename);
uint32_t EPUB3GetFileCountInArchive(EPUB3Ref epub);
EPUB3Error EPUB3GetUncompressedSizeOfFileInArchive(EPUB3Ref epub, uint32_t *uncompressedSize, const char *filename);
EPUB3Error EPUB3WriteCurrentArchiveFileToPath(EPUB3Ref epub, const char * path);
EPUB3Error EPUB3CreateNestedDirectoriesForFileAtPath(const char * path);
char * EPUB3CopyOfPathByAppendingPathComponent(const char * path, const char * componentToAppend);
char * EPUB3CopyOfPathByDeletingLastPathComponent(const char * path);


#define EPUB3_FREE_AND_NULL(__epub3_ptr_to_null) do { \
  if(__epub3_ptr_to_null != NULL) { \
    free(__epub3_ptr_to_null); \
    __epub3_ptr_to_null = NULL; \
  } \
} while(0);

#define EPUB3_XML_FREE_AND_NULL(__epub3_xml_ptr_to_null) do { \
  if(__epub3_xml_ptr_to_null != NULL) { \
    xmlFree(__epub3_xml_ptr_to_null); \
    __epub3_xml_ptr_to_null = NULL; \
  } \
} while(0);


#pragma mark - Hash function
// via: http://www.azillionmonkeys.com/qed/hash.html
#undef get16bits
#if (defined(__GNUC__) && defined(__i386__)) || defined(__WATCOMC__) || defined(_MSC_VER) || defined (__BORLANDC__) || defined (__TURBOC__)
#define get16bits(d) (*((const uint16_t *) (d)))
#endif

#if !defined (get16bits)
#define get16bits(d) ((((uint32_t)(((const uint8_t *)(d))[1])) << 8)\
                       +(uint32_t)(((const uint8_t *)(d))[0]) )
#endif

static inline uint32_t SuperFastHash(const char * data, int len)
{
  uint32_t hash = len, tmp;
  int rem;

  if (len <= 0 || data == NULL) return 0;

  rem = len & 3;
  len >>= 2;

  /* Main loop */
  for (;len > 0; len--) {
    hash  += get16bits (data);
    tmp    = (get16bits (data+2) << 11) ^ hash;
    hash   = (hash << 16) ^ tmp;
    data  += 2*sizeof (uint16_t);
    hash  += hash >> 11;
  }

  /* Handle end cases */
  switch (rem) {
    case 3: hash += get16bits (data);
      hash ^= hash << 16;
      hash ^= ((signed char)data[sizeof (uint16_t)]) << 18;
      hash += hash >> 11;
      break;
    case 2: hash += get16bits (data);
      hash ^= hash << 11;
      hash += hash >> 17;
      break;
    case 1: hash += (signed char)*data;
      hash ^= hash << 10;
      hash += hash >> 1;
  }

  /* Force "avalanching" of final 127 bits */
  hash ^= hash << 3;
  hash += hash >> 5;
  hash ^= hash << 4;
  hash += hash >> 17;
  hash ^= hash << 25;
  hash += hash >> 6;

  return hash;
}

#endif
