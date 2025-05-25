#define _POSIX_C_SOURCE 200809L
#ifndef EPUB3_h
#define EPUB3_h

#if defined(__cplusplus)
extern "C" {
#endif

#include <stdint.h>

typedef enum _EPUB3Error {
  kEPUB3Success = 0,
  kEPUB3UnknownError = 1001,
  kEPUB3InvalidArgumentError = 1002,
  kEPUB3InvalidMimetypeError = 1003,
  kEPUB3FileNotFoundInArchiveError = 1004,
  kEPUB3FileReadFromArchiveError = 1005,
  kEPUB3ArchiveUnavailableError = 1006,
  kEPUB3XMLReadFromBufferError = 1007,
  kEPUB3XMLParseError = 1008,
  kEPUB3XMLXElementNotFoundError = 1009,
  kEPUB3XMLXDocumentInvalidError = 1010,
  kEPUB3NCXNavMapEnd = 1011,
} EPUB3Error;

typedef enum { kEPUB3_NO = 0 , kEPUB3_YES = 1 } EPUB3Bool;

typedef struct EPUB3 * EPUB3Ref;
typedef struct EPUB3TocItem * EPUB3TocItemRef;

/* Creates and returns reference to an EPUB stored at path */
EPUB3Ref EPUB3CreateWithArchiveAtPath(const char * path, EPUB3Error *error);

/* Memory management */
void EPUB3Retain(EPUB3Ref epub);
void EPUB3Release(EPUB3Ref epub);

/* metadata copy and return functions */
char * EPUB3CopyTitle(EPUB3Ref epub);
char * EPUB3CopyAuthor(EPUB3Ref epub);
char * EPUB3CopyIdentifier(EPUB3Ref epub);
char * EPUB3CopyLanguage(EPUB3Ref epub);
char * EPUB3CopyCoverImagePath(EPUB3Ref epub);
char * EPUB3CopyMetaElementPathWithName(EPUB3Ref epub, const char * name);
char * EPUB3CopyMetaElementContentWithName(EPUB3Ref epub, const char * name);

/* builds an array of manifest items mathching a single required-module attribute */
void EPUB3ManifestFindItemsMatchingRequiredModuleWithName(EPUB3Ref epub, const char * moduleName, char ** matchingItems, int32_t matchSize);
    
/* locates cover image in epub and copies to bytes */
EPUB3Error EPUB3CopyCoverImage(EPUB3Ref epub, void ** bytes, uint32_t * byteCount);
/* given a epub file, writes it to a directory */
int SaveEPUBCoverToDir(const char *epubPath, const char *outputDir);
  
/* Returns count of linear items in OPF spine */
int32_t EPUB3CountOfSequentialResources(EPUB3Ref epub);
/* Adds the href attribute of linear spine resource to array of resources */
EPUB3Error EPUB3GetPathsOfSequentialResources(EPUB3Ref epub, const char ** resources);
/* Extracts epub archive to path  */
EPUB3Error EPUB3ExtractArchiveToPath(EPUB3Ref epub, const char * path);
/* in container.xml copied rootfile element full-path attribute into rootPath */
EPUB3Error EPUB3CopyRootFilePathFromContainer(EPUB3Ref epub, char ** rootPath);

/* TOC functions */
int32_t EPUB3CountOfTocRootItems(EPUB3Ref epub);
EPUB3Error EPUB3GetTocRootItems(EPUB3Ref epub, EPUB3TocItemRef *tocItems);
EPUB3Bool EPUB3TocItemHasParent(EPUB3TocItemRef tocItem);
EPUB3TocItemRef EPUB3TocItemGetParent(EPUB3TocItemRef tocItem);
int32_t EPUB3TocItemCountOfChildren(EPUB3TocItemRef tocItem);
EPUB3Error EPUB3TocItemGetChildren(EPUB3TocItemRef parent, EPUB3TocItemRef *children);
char * EPUB3TocItemCopyTitle(EPUB3TocItemRef tocItem);
char * EPUB3TocItemCopyPath(EPUB3TocItemRef tocItem);

/* Zip Entry */
EPUB3Error EPUB3CopyFileIntoBuffer_(EPUB3Ref epub, void **buffer, uint32_t *bufferSize, uint32_t *bytesCopied, const char * filename);


#if defined(__cplusplus)
} //EXTERN "C"
#endif

#endif
