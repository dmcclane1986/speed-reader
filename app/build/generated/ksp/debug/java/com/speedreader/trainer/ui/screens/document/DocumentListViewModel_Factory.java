package com.speedreader.trainer.ui.screens.document;

import com.speedreader.trainer.data.repository.DocumentRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class DocumentListViewModel_Factory implements Factory<DocumentListViewModel> {
  private final Provider<DocumentRepository> documentRepositoryProvider;

  public DocumentListViewModel_Factory(Provider<DocumentRepository> documentRepositoryProvider) {
    this.documentRepositoryProvider = documentRepositoryProvider;
  }

  @Override
  public DocumentListViewModel get() {
    return newInstance(documentRepositoryProvider.get());
  }

  public static DocumentListViewModel_Factory create(
      Provider<DocumentRepository> documentRepositoryProvider) {
    return new DocumentListViewModel_Factory(documentRepositoryProvider);
  }

  public static DocumentListViewModel newInstance(DocumentRepository documentRepository) {
    return new DocumentListViewModel(documentRepository);
  }
}
